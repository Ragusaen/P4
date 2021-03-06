package codegeneration

import ErrorHandler
import Helper.Companion.getFunParams
import sablecc.node.*
import symboltable.ScopedTraverser
import symboltable.SymbolTable
import typeChecking.Type
import java.util.*

class CodeGenerator(private val typeTable: MutableMap<Node, Type>, errorHandler: ErrorHandler, symbolTable: SymbolTable) : ScopedTraverser(errorHandler, symbolTable) {

    // The code stack is the stack used to 'bubble up' code. Pushing to this should be done after every node.
    private var codeStack = Stack<String>()
    // This method is a shorthand for applying a node and popping it off the stack. It generates a warning for debugging
    // if nothing was added to the stack.
    private fun getCode(node: Node): String {
        val size = codeStack.size
        node.apply(this)
        if (size == codeStack.size)
            println("Warning: Nothing was pushed to the codestack when getting code for node ${node::class.simpleName}\n")
        return codeStack.pop()
    }

    // These members control the indentation level while generating code
    private var indentLevel = 0
    private fun increaseIndent(n: Int = 1) { indentLevel += n }
    private fun decreaseIndent(n: Int = 1) { indentLevel -= n }
    private val singleIndent = "    "

    private fun getIndent():String = singleIndent.repeat(indentLevel)


    // This adds an extension method to the stack that automatically adds the appropriate indentation
    private fun Stack<String>.pushLineIndented(s:String, indentation:Int = indentLevel) = this.push(singleIndent.repeat(indentation) + s +"\n")
    private fun <T> Stack<T>.popAll(): List<T> {
        val list = toList()
        clear()
        return list
    }

    private val taskPrefix = "Task"

	// This field is used to output prototypes for functions
    private val functionPrototypes = mutableListOf<String>()

    // The tick length is the length of a FreeRTOS time-slice
    private val ticklengthms = 15

    // This field keeps track of the name of the module that is currently being generated code for.
    // It is used to remember the module name while traversing e.g. the inner module
    private var currentModuleName = ""

    // These are auxiliary objects to allow retention of code, such that it can be emitted in the setup or top of code.
    private val moduleAuxes = mutableListOf<ModuleAux>()
    private val templateInstances = mutableMapOf<String, MutableList<TemplateInstance>>()

    // This field stores the code that is in the init block, and should be emitted into setup
    private var initCode = ""

    // This remembers declarations that should be emitted in the top code.
    private val topDcls = mutableListOf<String>()

    // This method generates the code for the setup function.
    private fun generateSetup(): String {
        var res = "void setup() {\n"

        // First the code for setting up the modules is emitted
        for (ma in moduleAuxes) {
            if (ma is TemplateModuleAux) {
                if (templateInstances.containsKey(ma.name))
                    // Emit all instances of the given template module
                    for ((i, tmp) in templateInstances[ma.name]!!.withIndex()) {
                        res += "xTaskCreate(" +
                                taskPrefix + ma.name +
                                ", \"${ma.name}_${tmp.name}\", 128, (void*)&(TempMod_${ma.name}[$i]), 0, &TempMod_${ma.name}[$i].task_handle );\n" +
                                "vTaskSuspend(TempMod_${ma.name}[$i].task_handle);\n"
                }
            } else {
                res += "xTaskCreate(" +
                        taskPrefix + ma.name +
                        ", \"${ma.name}\", 128, NULL, 0, &${taskPrefix + ma.name}_Handle );\n" +
                        "vTaskSuspend(${taskPrefix + ma.name}_Handle);\n"
            }
        }

        // Create the controller task
        res += "xTaskCreate(ControllerTask, \"Controller\", 128, NULL, 0, NULL);\n"

        // Emit the stored init code
        res += initCode

        res += "}\n"
        return res
    }

    // This method generates the code for the controller task. The controller task contains all the logic to decide when
    // modules have fulfilled their condition to run.
    private fun generateControllerTask(): String {
        var res = "void ControllerTask(void *pvParameters) {\n"

        // Generate variables that stores the last value for the module.
        for (ma in moduleAuxes) {
            // Every needs an unsigned long to remember the last time it was scheduled; On needs a bool to remember the
            // last value the condition was evaluated to
            val lvType = if (ma.isEveryStruct) "unsigned long" else "Bool"

            if (ma is TemplateModuleAux) {
                // If it is a template module, then generate an array of last values, but only if there's an instance of
                // it
                if (!templateInstances.containsKey(ma.name))
                    continue

                // Get the amount of zeroes needed to initialise the array
                val c = templateInstances[ma.name]!!.size
                val zeroes = "0,".repeat(c).dropLast(1)

                res += "$lvType ${ma.name}LastValue[] = {$zeroes};\n" +
                        "struct ${ma.name}_t *${ma.name};"
            } else {
                res += "$lvType ${ma.name}LastValue = 0;\n"
            }
        }

        // The main infinite loop of the controller task. The variable e stores evaluated expressions in on modules; this
        // is to prevent evaluating them multiple times
        res += "\nwhile (1) {\nint e;\n"

        // Generate code for each module structure (on / every)
        for (ma in moduleAuxes) {
            res += ma.prefixCode + "\n"
            if (ma is TemplateModuleAux) {
                if (!templateInstances.containsKey(ma.name))
                    continue
                for ((i, tma) in templateInstances[ma.name]!!.withIndex()) {
                    // Get this modules environment
                    res += "${ma.name} = &TempMod_${ma.name}[$i];\n"

                    // Output an if statement that checks that the condition is met and the module is running, and on true
                    // resumes the task.
                    if (ma.isEveryStruct) {
                        res +=  "if (${ma.name}->running && millis() - ${ma.name}LastValue[$i] >= ${ma.expr}) {\n" +
                                "${ma.name}LastValue[$i] = millis();\n" +
                                "vTaskResume(${ma.name}->task_handle);\n}\n"
                    } else {
                        res +=  "e = ${ma.expr};\n" +
                                "if (${ma.name}->running && e && !${ma.name}LastValue[$i]) {\n" +
                                "vTaskResume(${ma.name}->task_handle);\n" +
                                "}\n${ma.name}LastValue[$i] = e;\n"
                    }
                }
            } else { // Instance module, same as template, just other name bindings
                if (ma.isEveryStruct) {
                    res += "if (Task${ma.name}_running && millis() - ${ma.name}LastValue >= ${ma.expr}) {\n" +
                            "${ma.name}LastValue = millis();\n" +
                            "vTaskResume(Task${ma.name}_Handle);\n}\n"
                } else {
                    res += "e = ${ma.expr};\n" +
                            "if (Task${ma.name}_running && e && !${ma.name}LastValue) {\n" +
                            "vTaskResume(Task${ma.name}_Handle);\n}\n" +
                            "${ma.name}LastValue = e;\n"
                }
            }
        }

        res += "}\n}\n"
        return res
    }


    // This method generates code for the top of the program file. Here declarations and other things that must appear
    // first is emitted
    private fun generateTopCode(): String {
        // Generate libraries
        var res = "#include <dumpling.h>\n\n"

        // Generate function prototypes
        res += functionPrototypes.joinToString("\n")
        res += "\n"

        // Generate global variables
        res += topDcls.joinToString("\n")
        res += "\n"

        // Generate code for modules
        for (ma in moduleAuxes) {
            if (ma is TemplateModuleAux) {
                if (!templateInstances.containsKey(ma.name))
                    continue

                // Output the definition of the struct
                res += ma.structDefinition + "\n"

                // Create array of initialized structs
                res += "struct ${ma.name}_t TempMod_${ma.name}[] = {\n"
                val initializers = mutableListOf<String>()
                for (tmpi in templateInstances[ma.name]!!) {
                    // Create an initialiser from templates declarations and the arguments provided in the module
                    // instance declaration
                    val args = tmpi.arguments.joinToString(",")
                    val v = "{" + listOf(args, ma.dclInits.joinToString(",")).filter {it.isNotEmpty()}.joinToString(",") + ",0,1}" // Add final 0 and 1 for task handle and running
                    initializers.add(v)
                }
                res += initializers.joinToString (",\n") + "\n};\n"
            } else {
                res += "TaskHandle_t Task${ma.name}_Handle;\nBool Task${ma.name}_running = 1;\n"
            }
        }

        return res
    }

    // Generate code for the entire program
    fun generate(startNode: Start): String {
        // Traverse the AST
        caseStart(startNode)

        // Add the extra code
        codeStack.push(generateControllerTask())
        codeStack.push(generateSetup())

        var output = generateTopCode()

        // Join it all together and return it
        for (codeSegment in codeStack)
            output += codeSegment
        output += "\n\n void loop() {}"
        return output
    }

// --------------------------------------------------------
// Root elements
// --------------------------------------------------------
    override fun caseAFunctiondcl(node: AFunctiondcl) {
        inAFunctiondcl(node)
        val identifier = getCode(node.identifier)
        val type = if (node.type == null) "void" else getCode(node.type)

        var param = ""
        if (node.param.size > 0) {
            param += getCode(node.param.first())
            for (p in node.param.drop(1)) {
                param += ", " + getCode(p)
            }
        }

        functionPrototypes.add("$type $identifier ($param);")

        val body = getCode(node.body)
        if (node.body is ABlockStmt)
            codeStack.push("$type $identifier ($param)\n$body")
        else
            codeStack.push("$type $identifier ($param){\n$body}\n")
        outAFunctiondcl(node)
    }

    override fun caseAParam(node: AParam) {
        val identifier = getCode(node.identifier)
        val type = getCode(node.type)

        codeStack.push("$type $identifier")
    }

    override fun caseATemplateModuledcl(node: ATemplateModuledcl) {
        inATemplateModuledcl(node)
        val name = node.identifier.text

        val innerModule = node.innerModule as AInnerModule

        // Create the struct
        var struct = "struct ${name}_t {\n"
        // First add the formal parameters
        for (param in node.param.map {it as AParam}) {
            val type = getCode(param.type)
            struct += "$type ${param.identifier.text};\n"
        }

        // Get all variables from the inner module
        for (dcl in innerModule.dcls.map {it as ADclStmt}) {
            val type = getCode(dcl.type)
            for (vdcl in dcl.vardcl.map {it as AVardcl}) {
                struct += "$type ${vdcl.identifier};\n"
            }
        }
        struct += "TaskHandle_t task_handle;\nBool running;};"

        // Run the inner module case
        caseAInnerModuleTemplate(innerModule, name, struct)
        val inner = codeStack.pop()

        codeStack.push("void Task$name(void *pvParameters) {\n" +
                "struct ${name}_t *$name = (struct ${name}_t*)pvParameters;\n" +
                "$inner\n" +
                "}\n")

        outATemplateModuledcl(node)
    }

    private fun caseAInnerModuleTemplate(node: AInnerModule, name: String, structDefinition: String) {
        node.moduleStructure.apply(this) // Pushes twice

        val mstruct = codeStack.pop()

        val expr = codeStack.pop()
        val prefixCode = blockPreviousLineInjection.popAll().joinToString("\n${getIndent()}")


        // Get the exprs for initializing, substitute with 0 if non-existent
        val dclInits = node.dcls.flatMap { (it as ADclStmt).vardcl }.map {if ((it as AVardcl).expr == null) "0" else getCode(it.expr)}

        moduleAuxes.add(TemplateModuleAux(dclInits, structDefinition, name, expr, node.moduleStructure is AEveryModuleStructure, prefixCode))

        codeStack.push("${singleIndent}while (1) {\n" +
                "$mstruct" +
                "${singleIndent.repeat(2)}vTaskSuspend(${name}->task_handle);\n" +
                "$singleIndent}")
    }

    override fun caseAInstanceModuledcl(node: AInstanceModuledcl) {
        inAInstanceModuledcl(node)
        val name = symbolTable.findModule(node)!!.first
        val inner = getCode(node.innerModule)

        codeStack.push("void Task$name(void *pvParameters) {\n$inner\n}\n")

        outAInstanceModuledcl(node)
    }

    override fun caseAInnerModule(node: AInnerModule) {
        val (moduleName, template) = symbolTable.findModule(node.parent())!!
        currentModuleName = moduleName

        val dcls = node.dcls.joinToString("\n") { getCode(it) }

        node.moduleStructure.apply(this) // Pushes twice

        increaseIndent()
        val mStruct = codeStack.pop()
        decreaseIndent()

        val expr = codeStack.pop()
        val prefixCode = blockPreviousLineInjection.joinToString("\n") { it }

        moduleAuxes.add(ModuleAux(moduleName, expr, node.moduleStructure is AEveryModuleStructure, prefixCode))

        codeStack.push(dcls)
        codeStack.push("${singleIndent}while (1) {\n" +
                mStruct +
                "${singleIndent.repeat(2)}vTaskSuspend(${taskPrefix}${moduleName}_Handle);\n" +
                "$singleIndent}")
    }

    override fun caseAEveryModuleStructure(node: AEveryModuleStructure) {
        val expr = getCode(node.expr)
        increaseIndent(2)
        val body = getCode(node.body)
        decreaseIndent(2)

        codeStack.push(expr)
        codeStack.push(body)
    }

    override fun caseAOnModuleStructure(node: AOnModuleStructure) {
        val expr = getCode(node.expr)
        increaseIndent(2)
        val body = getCode(node.body)
        decreaseIndent(2)

        codeStack.push(expr)
        codeStack.push(body)
    }


    override fun caseADclRootElement(node: ADclRootElement) {
        codeStack.push(getCode(node.stmt))
    }

// --------------------------------------------------------
// Expressions (non-trivial)
// --------------------------------------------------------
    override fun caseAUnopExpr(node: AUnopExpr) {
        val expr = getCode(node.expr)
        val unop = getCode(node.unop)

        codeStack.push("${unop}${expr}")
    }

    override fun caseABinopExpr(node: ABinopExpr) {
        val operator = getCode(node.binop)
        val r = getCode(node.r)
        val l = getCode(node.l)

        // Special case for strings where function calls must be made
        if(typeTable[node] == Type.String) {
            if (operator == "+")
                codeStack.push("concatstr($l, $r)")
            else
                throw java.lang.Exception()
        }
        else if(operator == "==" && typeTable[node.l] == Type.String && typeTable[node.r] == Type.String) {
            codeStack.push("equalstr($l, $r)")
        }
        else {
            codeStack.push(l + operator + r)
        }
    }

    override fun caseTTimeliteral(node: TTimeliteral) {
        super.caseTTimeliteral(node)

        // Convert the time to milliseconds
        val li = node.text.indexOfFirst { it.isLetter() }
        val num = node.text.substring(0, li).toFloat()
        val suffix = node.text.substring(li)

        val value = when(suffix) {
            "h" -> num * 3600000
            "m" -> num * 60000
            "s" -> num * 1000
            "ms" -> num
            else -> throw Exception("Unexpected suffix $suffix of time literal: ${node.text}") // This shouldn't happen
        }

        codeStack.push(value.toInt().toString())
    }

    override fun caseAIdentifierValue(node: AIdentifierValue) {
        val name = getCode(node.identifier)
        val lookup = symbolTable.findVar(name)
        if (lookup != null)
            codeStack.push(lookup.outName)
        else
            codeStack.push(name)
    }

    override fun caseAIndexExpr(node: AIndexExpr) {
        val index = getCode(node.index)
        val value = getCode(node.value)

        codeStack.push("$value[$index]")
    }

    override fun caseAReadExpr(node: AReadExpr) {
        val pin = getCode(node.pin)

        if (typeTable[node] == Type.Bool) {
            // Only switch pinmode if not reading a digital output pin
            if (!typeTable[node.pin]!!.exactlyEquals(Type.DigitalOutputPin))
                blockPreviousLineInjection.push("pinMode($pin, INPUT);")

            codeStack.push("digitalRead($pin)")
        }
        else
            codeStack.push("analogRead($pin)")
    }

    override fun caseAParenthesisExpr(node: AParenthesisExpr) {
        codeStack.push("( ${getCode(node.expr)})")
    }

    override fun caseAArrayType(node: AArrayType) {
        codeStack.push(getCode(node.type) + "*")
    }

    override fun caseAArrayValue(node: AArrayValue) {
        val expressions = node.expr.map {getCode(it)}.joinToString(", ")
        codeStack.push("{ $expressions }")
    }

    override fun caseAFunctionCallExpr(node: AFunctionCallExpr) {
        val identifier = getCode(node.identifier)

        var arguments = ""
        if (node.expr.firstOrNull() != null) {
            arguments += getCode(node.expr.first())
            for (arg in node.expr.drop(1)) {
                arguments += ", " + getCode(arg)
            }
        }

        codeStack.push("$identifier($arguments)")
    }

// --------------------------------------------------------
// Statements (non-trivial)
// --------------------------------------------------------
    override fun caseAIfStmt(node: AIfStmt) {
        val cond = getCode(node.condition)
        var res = getIndent() + "if ($cond)\n"

        if (node.ifBody is ABlockStmt)
            res += getCode(node.ifBody)
        else {
            increaseIndent()
            res += getCode(node.ifBody)
            decreaseIndent()
        }

        if (node.elseBody != null) {
            res += getIndent() + "else\n"
            if (node.elseBody is ABlockStmt)
                res += getCode(node.elseBody)
            else {
                increaseIndent()
                res += getCode(node.elseBody)
                decreaseIndent()
            }
        }

        codeStack.push(res)
    }

    override fun caseAWhileStmt(node: AWhileStmt) {
        val cond = getCode(node.condition)
        var res = getIndent() + "while ($cond)\n"

        if (node.body is ABlockStmt)
            res += getCode(node.body)
        else {
            increaseIndent()
            res += getCode(node.body)
            decreaseIndent()
        }

        codeStack.push(res)
    }

    override fun caseAForStmt(node: AForStmt) {
        inAForStmt(node)

        val lower = getCode(node.lower)
        val upper = getCode(node.upper)
        val step = if (node.step == null) "1" else getCode(node.step)
        val v = node.identifier.text

        val body = getCode(node.body)

        codeStack.pushLineIndented("for (int $v = $lower; $v < $upper; $v += $step)\n$body")

        outAForStmt(node)
    }

    override fun caseADclStmt(node: ADclStmt) {
        val type = if (node.type is AArrayType)
            getCode((node.type as AArrayType).type)
        else
            getCode(node.type)

        val vardcls = node.vardcl.map {getCode(it)}.joinToString(", ")

        codeStack.pushLineIndented("$type $vardcls;")

        // If this is a global variable, save it for the top declarations
        if (node.parent() is ADclRootElement) {
            topDcls.add(codeStack.pop())
            codeStack.push("")
        }
    }

    override fun caseAAssignStmt(node: AAssignStmt) {
        val id = symbolTable.findVar(getCode(node.identifier))!!.outName
        val expr = getCode(node.expr)

        if (node.binop != null) {
            val binop = getCode(node.binop)
            codeStack.pushLineIndented("$id $binop= $expr;")
        } else {
            codeStack.pushLineIndented("$id = $expr;")
        }
    }

    override fun caseAVardcl(node: AVardcl) {
        val identifier = symbolTable.findVar(getCode(node.identifier))!!.outName

        // If it is an array, we need to do a bunch of annoying stuff
        if (typeTable[node]!!.isArray()) {
            if (node.expr != null) {
                // If there is an initialiser, C can figure out how to construct the array
                val expr = getCode(node.expr)
                codeStack.push("$identifier[] = $expr")
            } else {
                // If there is no initialiser, we get the size ourselves
                val typeNode = ((node.parent() as ADclStmt).type as AArrayType)
                val size = getCode(typeNode.size)

                codeStack.push("$identifier[$size]")
            }
        } else {
            // For non-arrays, simply get the code of the expression
            if (node.expr != null) {
                val expr = getCode(node.expr)
                codeStack.push("$identifier = $expr")
            } else {
                codeStack.push(identifier)
            }
        }
    }

    // This field is used to inject a line of code before the statement that is currently begin generated
    private val blockPreviousLineInjection = Stack<String>()
	
    override fun caseABlockStmt(node: ABlockStmt) {
        inABlockStmt(node)
        var block = getIndent() + "{\n"

        increaseIndent()
        for (s in node.stmt) {
            val code = getCode(s)
            if (blockPreviousLineInjection.isNotEmpty()) {
                blockPreviousLineInjection.popAll().forEach { block += getIndent() + it + "\n" }
            }

            block += code
        }
        decreaseIndent()

        block += getIndent() + "}\n"

        codeStack.push(block)
        outABlockStmt(node)
    }

    override fun caseABreakStmt(node: ABreakStmt?) {
        codeStack.pushLineIndented("break;")
    }

    override fun caseAContinueStmt(node: AContinueStmt?) {
        codeStack.pushLineIndented("continue;")
    }

    override fun caseAExprStmt(node: AExprStmt) {
        val expr = getCode(node.expr)
        codeStack.pushLineIndented("$expr;")
    }

    override fun caseAReturnStmt(node: AReturnStmt) {
        if (node.expr != null) {
            val expr = getCode(node.expr)
            codeStack.pushLineIndented("return $expr;")
        } else {
            codeStack.pushLineIndented("return;")
        }
    }

    override fun caseASetToStmt(node: ASetToStmt) {
        val pin = getCode(node.pin)
        val value = getCode(node.value)

        if (typeTable[node] == Type.AnalogOutputPin) {
            codeStack.pushLineIndented("${getIndent()}analogWrite($pin, $value);")
        } else {
            codeStack.pushLineIndented("pinMode($pin, OUTPUT);\n${getIndent()}digitalWrite($pin, $value);")
        }
    }

    override fun caseAInitRootElement(node: AInitRootElement) {
        initCode += getCode(node.stmt)
    }

    override fun caseADelayStmt(node: ADelayStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("vTaskDelay( ($expr) / $ticklengthms);")
    }

    override fun caseADelayuntilStmt(node: ADelayuntilStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("while ( !($expr) ) vTaskDelay(2);")
    }

    override fun caseAModuledclStmt(node: AModuledclStmt) {
        val templateName = node.template.text!!
        val instanceName = node.instance.text!!

        val args = node.expr.map { getCode(it) }

        val tmpi = TemplateInstance(instanceName, args)

        codeStack.push("")

        if (templateInstances[templateName] != null)
            templateInstances[templateName]!!.add(tmpi)
        else
            templateInstances[templateName] = mutableListOf(tmpi)
    }

    override fun caseAStopStmt(node: AStopStmt) {
        val name = node.identifier?.text ?: currentModuleName
        val template = symbolTable.findModule(name)

        if (template == name) { // Instance modules
            codeStack.pushLineIndented("Task${name}_running = 0;")
        } else { // Template module
            val index = symbolTable.getTemplateInstanceIndex(name)!!
            codeStack.pushLineIndented("TempMod_${template}[$index].running = 0;")
        }
    }

    override fun caseAStartStmt(node: AStartStmt) {
        val name = node.identifier?.text ?: currentModuleName
        val template = symbolTable.findModule(name)

        if (template == name) { // Instance modules
            codeStack.pushLineIndented("Task${name}_running = 1;")
        } else { // Template module
            val index = symbolTable.getTemplateInstanceIndex(name)!!
            codeStack.pushLineIndented("TempMod_${template}[$index].running = 1;")
        }
    }

    override fun caseACriticalStmt(node: ACriticalStmt) {
        increaseIndent()
        val body = getCode(node.body)
        decreaseIndent()

        codeStack.push(
                "${getIndent()}{taskENTER_CRITICAL();\n" +
                        body +
                        "${getIndent()}taskEXIT_CRITICAL();}\n"
        )
    }

    override fun caseASleepStmt(node: ASleepStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("delay($expr);")
    }

    override fun caseAUsleepStmt(node: AUsleepStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("delayMicroseconds($expr);")
    }

// --------------------------------------------------------------------------------
// Below here is just trivial 1-2 liners
// --------------------------------------------------------------------------------
    override fun caseADigitalinputpinType(node: ADigitalinputpinType?) {
        codeStack.push("DigitalInputPin")
    }

    override fun caseADigitaloutputpinType(node: ADigitaloutputpinType?) {
        codeStack.push("DigitalOutputPin")
    }

    override fun caseAAnaloginputpinType(node: AAnaloginputpinType?) {
        codeStack.push("AnalogOutputPin")
    }

    override fun caseAAnalogoutputpinType(node: AAnalogoutputpinType?) {
        codeStack.push("AnalogOutputPin")
    }

    override fun caseTDigitalpinliteral(node: TDigitalpinliteral) {
        super.caseTDigitalpinliteral(node)
        codeStack.push(node.text.substring(1))
    }

    override fun caseTAnalogpinliteral(node: TAnalogpinliteral) {
        super.caseTAnalogpinliteral(node)
        codeStack.push(node.text.drop(1))
    }

    override fun caseTAnaloginputpintype(node: TAnaloginputpintype) {
        super.caseTAnaloginputpintype(node)
        codeStack.push("int")
    }
    override fun caseTAnalogoutputpintype(node: TAnalogoutputpintype) {
        super.caseTAnalogoutputpintype(node)
        codeStack.push("int")
    }

    override fun caseTDigitalinputpintype(node: TDigitalinputpintype) {
        super.caseTDigitalinputpintype(node)
        codeStack.push("int")
    }

    override fun caseTDigitaloutputpintype(node: TDigitaloutputpintype) {
        super.caseTDigitaloutputpintype(node)
        codeStack.push("int")
    }

    override fun caseTStringliteral(node: TStringliteral) {
        super.caseTStringliteral(node)
        codeStack.push(node.text)
    }

    // Value handling
    override fun caseTIdentifier(node: TIdentifier) {
        super.caseTIdentifier(node)
        codeStack.push(node.text)
    }

    override fun caseAIntValue(node: AIntValue) {
        codeStack.push(node.intliteral.text)
    }

    override fun caseAFloatValue(node: AFloatValue) {
        codeStack.push(node.floatliteral.text)
    }

    override fun caseABoolValue(node: ABoolValue) {
        codeStack.push(node.boolliteral.text)
    }

    override fun caseATimeValue(node: ATimeValue) {
        node.timeliteral.apply(this)
    }

    // Type handling
    override fun caseAIntType(node: AIntType) {
        codeStack.push("int16_t")
    }

    override fun caseAInt8Type(node: AInt8Type) {
        codeStack.push("int8_t")
    }

    override fun caseAInt16Type(node: AInt16Type) {
        codeStack.push("int16_t")
    }

    override fun caseAInt32Type(node: AInt32Type) {
        codeStack.push("int32_t")
    }

    override fun caseAInt64Type(node: AInt64Type) {
        codeStack.push("int64_t")
    }

    override fun caseAUintType(node: AUintType) {
        codeStack.push("uint16_t")
    }

    override fun caseAUint8Type(node: AUint8Type) {
        codeStack.push("uint8_t")
    }

    override fun caseAUint16Type(node: AUint16Type) {
        codeStack.push("uint16_t")
    }

    override fun caseAUint32Type(node: AUint32Type) {
        codeStack.push("uint32_t")
    }

    override fun caseAUint64Type(node: AUint64Type) {
        codeStack.push("uint64_t")
    }

    override fun caseAFloatType(node: AFloatType) {
        codeStack.push("float")
    }

    override fun caseATimeType(node: ATimeType) {
        codeStack.push("Time")
    }

    override fun caseABoolType(node: ABoolType) {
        codeStack.push("Bool")
    }

    override fun caseAStringType(node: AStringType) {
        codeStack.push("char*")
    }

    // Operators for binop and unop
    override fun caseAAdditionBinop(node: AAdditionBinop) {
        codeStack.push("+")
    }

    override fun caseADivisionBinop(node: ADivisionBinop) {
        codeStack.push("/")
    }

    override fun caseAAndBinop(node: AAndBinop) {
        codeStack.push("&&")
    }

    override fun caseAEqualBinop(node: AEqualBinop) {
        codeStack.push("==")
    }

    override fun caseAGreaterthanBinop(node: AGreaterthanBinop) {
        codeStack.push(">")
    }

    override fun caseALessthanBinop(node: ALessthanBinop) {
        codeStack.push("<")
    }

    override fun caseAModuloBinop(node: AModuloBinop) {
        codeStack.push("%")
    }

    override fun caseAMultiplicationBinop(node: AMultiplicationBinop) {
        codeStack.push("*")
    }

    override fun caseAOrBinop(node: AOrBinop) {
        codeStack.push("||")
    }

    override fun caseASubtractionBinop(node: ASubtractionBinop?) {
        codeStack.push("-")
    }

    override fun caseAGreaterthanorequalBinop(node: AGreaterthanorequalBinop) {
        codeStack.push(">=")
    }

    override fun caseALessthanorequalBinop(node: ALessthanorequalBinop) {
        codeStack.push("<=")
    }

    override fun caseAMinusUnop(node: AMinusUnop) {
        codeStack.push("-")
    }

    override fun caseAPlusUnop(node: APlusUnop?) {
        codeStack.push("+")
    }

    override fun caseANotUnop(node: ANotUnop) {
        codeStack.push("!")
    }
}
