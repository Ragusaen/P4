package codegeneration

import ErrorHandler
import sablecc.node.*
import semantics.symbolTable.ScopedTraverser
import semantics.symbolTable.SymbolTable
import semantics.typeChecking.Type
import java.util.*

class CodeGenerator(private val typeTable: MutableMap<Node, Type>, errorHandler: ErrorHandler, symbolTable: SymbolTable) : ScopedTraverser(errorHandler, symbolTable) {
    private object Emitter {
        private var setupCode = ""
        private var globalCode = ""
        private var loopCode = ""

        fun emitSetup(code: String) {
            setupCode += code
        }
        fun emitGlobal(code: String) {
            globalCode += code
        }
        fun emitLoop(code: String) {
            loopCode += code
        }

        fun finalize(): String ="$globalCode \n void loop() {}\n"
    }

    private var indentLevel = 0

    private fun increaseIndent(n: Int = 1) { indentLevel += n }
    private fun decreaseIndent(n: Int = 1) { indentLevel -= n }

    private val singleIndent = "    "

    private val taskPrefix = "Task"
    private val tickratems = 15

    private fun Stack<String>.pushLineIndented(s:String, indentation:Int = indentLevel) = codeStack.push(singleIndent.repeat(indentation) + s +"\n")

    private fun getIndent():String = singleIndent.repeat(indentLevel)

    private var currentModuleName = ""

    private fun toSimpleCode(s:String):String {
        return s.trim().trim { it == ';' }
    }

    private var codeStack = Stack<String>()

    private val moduleAuxes = mutableListOf<ModuleAux>()
    private val templateInstances = mutableMapOf<String, MutableList<String>>()
    private var initCode = ""

    private fun generateSetup(): String {
        var res = "void setup() {\n"
        moduleAuxes.forEach { res += "xTaskCreate(" +
                taskPrefix + it.name +
                ", \"${it.name}\", 128, NULL, 0, &${taskPrefix + it.name}_Handle );\n" +
                "vTaskSuspend(${taskPrefix + it.name}_Handle);\n" }

        res += "xTaskCreate(ControllerTask, \"Controller\", 128, NULL, 0, NULL);\n"

        res += initCode

        res += "}\n"
        return res
    }

    private fun generateControllerTask(): String {
        var res = "void ControllerTask(void *pvParameters) {\n"

        moduleAuxes.forEach { res += "${if (it.isEveryStruct) "unsigned long" else "Bool"} ${it.name}LastValue = 0;\n" }

        res += "\nwhile (1) {\n"

        for (ima in moduleAuxes) {
            if (ima.isEveryStruct) {
                res += "if (millis() - ${ima.name}LastValue >= ${ima.expr}) {\n${ima.name}LastValue = millis();\nvTaskResume(Task${ima.name}_Handle);\n}\n"
            } else {
                res += "if (${ima.expr} && !${ima.name}LastValue) {\nvTaskResume(Task${ima.name}_Handle);\n}\n${ima.name}LastValue = ${ima.expr};\n"
            }
        }

        res += "}\n}\n"
        return res
    }

    private fun generateTopCode(): String {
        var res = "#include <Arduino_FreeRTOS.h>\n\ntypedef char Bool;\ntypedef unsigned int Time;\n"

        for (ma in moduleAuxes) {
            if (ma.isTemplate) {
                res += "struct ${ma.name}_t Mod${ma.name}[] = {"
            } else {
                res += "TaskHandle_t Task${ma.name}_Handle;\n"
            }
        }

        return res
    }

    fun generate(startNode: Start): String {
        caseStart(startNode)
        codeStack.push(generateControllerTask())
        codeStack.push(generateSetup())
        Emitter.emitGlobal(generateTopCode())
        for (i in codeStack)
            Emitter.emitGlobal(i)
        return Emitter.finalize()
    }

    private fun getCode(node: Node): String {
        val size = codeStack.size
        node.apply(this)
        if (size == codeStack.size)
            println("Warning: Nothing was pushed to the codestack when getting code for node $node\n")
        return codeStack.pop()
    }

    override fun caseAUnopExpr(node: AUnopExpr) {
        val expr = getCode(node.expr)
        val unop = getCode(node.unop)

        codeStack.push("${unop}${expr}")
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

        codeStack.pushLineIndented("for (Int $v = $lower; $v < $upper; $v += $step)\n$body")

        outAForStmt(node)
    }

    override fun caseADclStmt(node: ADclStmt) {
        val type = if (node.type is AArrayType)
            getCode((node.type as AArrayType).type)
        else
            getCode(node.type)

        var vardcls = getCode(node.vardcl.first())
        for (v in node.vardcl.drop(1)) {
            vardcls += ", " + getCode(v)
        }
        codeStack.pushLineIndented("$type $vardcls;")
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

        if (typeTable[node]!!.isArray()) {
            if (node.expr != null) {
                val expr = getCode(node.expr)
                codeStack.push("$identifier[] = $expr")
            } else {
                val typeNode = ((node.parent() as ADclStmt).type as AArrayType)
                val size = getCode(typeNode.size)
                val eType = getCode(typeNode.type)

                val cec = ConstantExpressionChecker()
                typeNode.size.apply(cec)
                if (cec.isConstant) {
                    codeStack.push("$identifier[$size]")
                } else {
                    codeStack.push("*$identifier = malloc($size * sizeof($eType))")
                }
            }
        } else {
            if (node.expr != null) {
                val expr = getCode(node.expr)
                codeStack.push("$identifier = $expr")
            } else {
                codeStack.push(identifier)
            }
        }
    }
	
    override fun caseABlockStmt(node: ABlockStmt) {
        inABlockStmt(node)
        var block = getIndent() + "{\n"

        increaseIndent()
        for (s in node.stmt) {
            block += getCode(s)
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


    // Immediate handling
    override fun caseTTimeliteral(node: TTimeliteral) {
        super.caseTTimeliteral(node)

        val li = node.text.indexOfFirst { it.isLetter() }
        val num = node.text.substring(0, li).toFloat()
        val suffix = node.text.substring(li)

        val value = when(suffix) {
            "h" -> num * 3600000
            "m" -> num * 60000
            "s" -> num * 1000
            "ms" -> num
            else -> throw Exception("Unexpected suffix $suffix of time literal: ${node.text}")
        }

        codeStack.push(value.toInt().toString())
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
        codeStack.push("int")
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

    override fun caseAIdentifierValue(node: AIdentifierValue) {
        val name = getCode(node.identifier)
        val lookup = symbolTable.findVar(name)
        if (lookup != null)
            codeStack.push(lookup.outName)
        else
            codeStack.push(name)
    }

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

        val body = getCode(node.body)
        codeStack.push("$type $identifier ($param)\n$body")
        outAFunctiondcl(node)
    }

    override fun caseAParam(node: AParam) {
        val identifier = getCode(node.identifier)
        val type = getCode(node.type)

        codeStack.push("$type $identifier")
    }

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

    override fun caseAFunctionCallExpr(node: AFunctionCallExpr) {
        val identifier = getCode(node.identifier)

        var arguments = ""
        if (node.expr != null) {
            arguments += getCode(node.expr.first())
            for (arg in node.expr.drop(1)) {
                arguments += ", " + getCode(arg)
            }
        }

        codeStack.push("$identifier($arguments)")
    }

    override fun caseATemplateModuledcl(node: ATemplateModuledcl) {
        inATemplateModuledcl(node)
        val name = node.identifier.text

        val innerModule = node.innerModule as AInnerModule

        // Create the struct
        var res = "struct ${name}_t {\n"
        // First add the formal parameters
        for (param in node.param.map {it as AParam}) {
            val type = getCode(param.type)
            res += "$type ${param.identifier.text}"
        }

        // Get all variables from the inner module
        for (dcl in innerModule.dcls.map {it as ADclStmt}) {
            val type = getCode(dcl.type)
            for (vdcl in dcl.vardcl.map {it as AVardcl}) {
                res += "$type ${vdcl.identifier};\n"
            }
        }
        res += "TaskHandler task_handle;\n};\n"

        // Run the inner module case
        caseAInnerModuleTemplate(innerModule, name)
        val inner = codeStack.pop()

        codeStack.push(res + "void Task$name(void *pvParameters) {\n" +
                "struct ${name}_t *$name = (struct ${name}_t*)pvParameters)\n" +
                "$inner\n" +
                "}\n")

        outATemplateModuledcl(node)
    }

    private fun caseAInnerModuleTemplate(node: AInnerModule, name: String) {
        node.moduleStructure.apply(this) // Pushes twice

        increaseIndent()
        val mstruct = codeStack.pop()
        decreaseIndent()

        val expr = codeStack.pop()

        moduleAuxes.add(ModuleAux(name, expr, node.moduleStructure is AEveryModuleStructure, true))

        codeStack.push("${singleIndent}while (1) {\n" +
                "$mstruct" +
                "${singleIndent.repeat(2)}vTaskSuspend(${taskPrefix}${name}->task_handle);\n" +
                "$singleIndent}")
    }

    override fun caseAInstanceModuledcl(node: AInstanceModuledcl) {
        inAInstanceModuledcl(node)
        val name = symbolTable.findModule(node)
        val inner = getCode(node.innerModule)

        codeStack.push("void Task$name(void *pvParameters) {\n$inner\n}\n")

        outAInstanceModuledcl(node)
    }

    override fun caseAInnerModule(node: AInnerModule) {
        val moduleName = symbolTable.findModule(node.parent())!!
        currentModuleName = moduleName

        val dcls = node.dcls.map {getCode(it)}.joinToString("\n")

        node.moduleStructure.apply(this) // Pushes twice

        increaseIndent()
        val mstruct = codeStack.pop()
        decreaseIndent()

        val expr = codeStack.pop()

        moduleAuxes.add(ModuleAux(moduleName, expr, node.moduleStructure is AEveryModuleStructure, false))

        codeStack.push(dcls)
        codeStack.push("${singleIndent}while (1) {\n" +
                "$mstruct" +
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

    override fun caseAArrayType(node: AArrayType) {
        codeStack.push(getCode(node.type) + "*")
    }

    override fun caseAArrayValue(node: AArrayValue) {
        val exprs = node.expr.map {getCode(it)}.joinToString(", ")
        codeStack.push("{ $exprs }")
    }

    override fun caseAIndexExpr(node: AIndexExpr) {
        val index = getCode(node.index)
        val value = getCode(node.value)

        codeStack.push("$value[$index]")
    }

    override fun caseADclRootElement(node: ADclRootElement) {
        codeStack.push(getCode(node.stmt))
    }

    override fun caseASetToStmt(node: ASetToStmt) {
        val pin = getCode(node.pin)
        val value = getCode(node.value)

        if (typeTable[node] == Type.AnalogOutputPin) {
            codeStack.pushLineIndented("analogWrite($pin, $value);")
        } else {
            codeStack.pushLineIndented("digitalWrite($pin, $value);")
        }
    }

    override fun caseAReadExpr(node: AReadExpr) {
        val pin = getCode(node.pin)

        if (typeTable[node] == Type.Bool)
            codeStack.push("digitalRead($pin)")
        else
            codeStack.push("analogRead($pin)")
    }

    override fun caseTDigitalpinliteral(node: TDigitalpinliteral) {
        super.caseTDigitalpinliteral(node)
        codeStack.push(node.text.substring(1))
    }

    override fun caseTAnalogpinliteral(node: TAnalogpinliteral) {
        super.caseTAnalogpinliteral(node)
        codeStack.push(node.text)
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

    override fun caseAInitRootElement(node: AInitRootElement) {
        initCode += getCode(node.stmt)
    }

    override fun caseADelayStmt(node: ADelayStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("vTaskDelay( ($expr) / $tickratems);")
    }

    override fun caseADelayuntilStmt(node: ADelayuntilStmt) {
        val expr = getCode(node.expr)

        codeStack.pushLineIndented("while ( !($expr) ) vTaskDelay(2);")
    }

    override fun caseAModuledclStmt(node: AModuledclStmt) {
        val templateName = node.template.text!!
        val instanceName = node.instance.text!!

        if (templateInstances[templateName] != null)
            templateInstances[templateName]!!.add(instanceName)
        else
            templateInstances[templateName] = mutableListOf(instanceName)
    }

    override fun caseAStopStmt(node: AStopStmt) {
        if (node.identifier != null) {
            codeStack.pushLineIndented("vTaskSuspend($taskPrefix${node.identifier.text}_Handle);")
        } else {
            codeStack.pushLineIndented("vTaskSuspend($taskPrefix${currentModuleName}_Handle);")
        }
    }

}