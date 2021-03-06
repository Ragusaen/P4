package typeChecking

import ErrorHandler
import sablecc.node.*
import symboltable.ScopedTraverser
import symboltable.SymbolTable
import typeChecking.errors.*
import java.util.*

class TypeChecker(errorHandler: ErrorHandler, symbolTable: SymbolTable) : ScopedTraverser(errorHandler, symbolTable) {
    fun run(node: Start): MutableMap<Node, Type> {
        caseStart(node)
        return typeTable
    }

    private val typeStack = Stack<Type>()

    private val typeTable = mutableMapOf<Node, Type>()

    private fun pushType(node: Node, type: Type) {
        typeStack.push(type)
        typeTable[node] = type
    }

    private fun getType(node: Node): Type {
        val prevSize = typeStack.size

        node.apply(this)

        if (typeStack.size <= prevSize)
            println("Warning: Nothing was pushed to the typestack when getting type for node ${node::class.simpleName}\n")
        return typeStack.pop()
    }

    private var currentFunctionReturnType: Type? = null
    private var currentFunctionName:String? = null

    override fun inAFunctiondcl(node: AFunctiondcl) {
        super.inAFunctiondcl(node)

        currentFunctionReturnType = symbolTable.findFun(node.identifier.text, Helper.getFunParams(node))!!.type
        currentFunctionName = node.identifier.text
    }

    override fun outAReturnStmt(node: AReturnStmt) {
        val type = if (node.expr == null) Type.Void else typeStack.pop()

        if (currentFunctionReturnType != type) {
            error(IllegalImplicitTypeConversionError(
                    "In function $currentFunctionName:\n" +
                    "Expected return statement of type $currentFunctionReturnType, but got $type."))
        }
    }

    override fun outAModuledclStmt(node: AModuledclStmt) {
        val templateId = symbolTable.findTemplateModule(symbolTable.findModule(node)!!.second)
                ?: error(IdentifierNotDeclaredError("Module with name ${node.template.text} does not exist"))

        // Pop the expressions from the typeStack
        val types = mutableListOf<Type>()
        for (i in 0 until node.expr.size)
            types.add(typeStack.pop())

        // The expressions are popped in reverse order
        types.reverse()

        if (templateId.paramTypes != types)
            error(IllegalImplicitTypeConversionError("Module ${node.instance.text} expects types ${templateId.paramTypes}, but got $types"))
    }

    override fun outAEveryModuleStructure(node: AEveryModuleStructure) {
        super.outAEveryModuleStructure(node)
        val conditionType = typeStack.pop()

        errorHandler.setLineAndPos(node.token)

        if (conditionType != Type.Time)
            error(IllegalImplicitTypeConversionError("Every expects expression of type Time, but got $conditionType"))
    }

    override fun outAOnModuleStructure(node: AOnModuleStructure) {
        super.outAOnModuleStructure(node)
        val conditionType = typeStack.pop()

        errorHandler.setLineAndPos(node.token)

        if (conditionType != Type.Bool)
            error(IllegalImplicitTypeConversionError("On expects expression of type Bool, but got $conditionType"))
    }

    override fun outAIfStmt(node: AIfStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.Bool)
            error(IllegalImplicitTypeConversionError("'If' expects expression of type Bool, but got $conditionType"))
    }

    override fun outAForStmt(node: AForStmt) {
        super.outAForStmt(node)

        val stepType = if (node.step == null) Type.Void else typeStack.pop()
        val upperType = typeStack.pop()
        val lowerType = typeStack.pop()

        if (!lowerType.isIntType())
            error(IllegalImplicitTypeConversionError("'For expects lower bound of type Int, but got $lowerType"))

        if (!upperType.isIntType())
            error(IllegalImplicitTypeConversionError("'For expects upper bound of type Int, but got $upperType"))

        if (node.step != null && !stepType.isIntType())
            error(IllegalImplicitTypeConversionError("'For expects step of type Int, but got $stepType"))
    }

    override fun outAWhileStmt(node: AWhileStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.Bool)
            error(IllegalImplicitTypeConversionError("'While' expects expression of type Bool, but got $conditionType"))
    }

    override fun outAExprStmt(node: AExprStmt) {
        typeStack.pop() // Throw type away
    }

    override fun outAFunctionCallExpr(node: AFunctionCallExpr) {
        val name = node.identifier.text
        // Pop the expressions from the typeStack
        val types = mutableListOf<Type>()
        for (i in 0 until node.expr.size)
            types.add(typeStack.pop())

        // The expressions are popped in reverse order
        types.reverse()

        val id = symbolTable.findFun(name, types)
        if (id != null) {
            pushType(node, id.type)
        }
        else {
            if (types.size > 0)
                error(IdentifierNotDeclaredError("Function with the name $name and parameter types ${types.joinToString(", ")} does not exist"))
            else
                error(IdentifierNotDeclaredError("Function with the name $name and no parameters does not exist"))
        }
    }

    override fun outABinopExpr(node: ABinopExpr) {
        val rType = typeStack.pop()
        val lType = typeStack.pop()
        val op = node.binop

        val returnType = OperatorType.getReturnType(lType, op, rType)
                ?: error(IncompatibleOperatorError("Cannot apply binary operator ${OperatorType.opNodeToString(op)} between types $lType and $rType"))

        pushType(node, returnType)
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = symbolTable.findVar(node.identifier.text)!!

        // Add to type table
        typeTable[node] = identifier.type

        if (identifier.type.isArray()) {
            val typeNode = ((node.parent() as ADclStmt).type as AArrayType)
            if (typeNode.size == null && node.expr == null) {
                error(ArrayInitializationError("Cannot declare array ${node.identifier.text} with no size parameters."))
            }
        }

        if (node.expr != null) {
            val typeE = typeStack.pop()
            if (typeE != identifier.type)
                if ((identifier.type == Type.AnalogOutputPin || identifier.type == Type.AnalogInputPin) && typeE != Type.AnalogPin)
                    error(IllegalImplicitTypeConversionError("Cannot assign type $typeE to an analog pin."))
                else if ((identifier.type == Type.DigitalOutputPin || identifier.type == Type.DigitalInputPin) && typeE != Type.DigitalPin)
                    error(IllegalImplicitTypeConversionError("Cannot assign type $typeE to a digital pin."))
                else
                    error(IllegalImplicitTypeConversionError("Cannot initialize the variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE."))
        }
    }

    override fun outAUnopExpr(node: AUnopExpr) {
        val exprType = typeStack.peek()

        when(node.unop) {
            is ANotUnop -> if (exprType != Type.Bool)
                error(IncompatibleOperatorError("Cannot apply conditional unary '!' operator to expression of $exprType"))
            is APlusUnop -> if (!exprType.isIntType() && exprType != Type.Float && exprType != Type.Time)
                error(IncompatibleOperatorError("Cannot apply conditional unary '+' operator to expression of $exprType"))
            is AMinusUnop -> if (!exprType.isIntType() && exprType != Type.Float && exprType != Type.Time)
                error(IncompatibleOperatorError("Cannot apply conditional unary '-' operator to expression of $exprType"))
        }
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        val typeExpr = typeStack.pop()
        val typeId = symbolTable.findVar(node.identifier.text)!!.type

        if (typeExpr != typeId){
            error(IllegalImplicitTypeConversionError("Cannot assign variable ${node.identifier.text} of type $typeId with value of type $typeExpr"))
        }
    }

    // This is only for variables used in expressions as values
    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = symbolTable.findVar(node.identifier.text)
        pushType(node, identifier!!.type)
    }

    override fun outAValueExpr(node: AValueExpr) {
        pushType(node, typeStack.pop())
    }

    override fun caseTIntliteral(node: TIntliteral) {
        super.caseTIntliteral(node)
        pushType(node, Type.Int)
    }

    override fun caseTFloatliteral(node: TFloatliteral) {
        super.caseTFloatliteral(node)
        pushType(node, Type.Float)
    }

    override fun caseTBoolliteral(node: TBoolliteral) {
        super.caseTBoolliteral(node)
        pushType(node, Type.Bool)
    }

    override fun caseTStringliteral(node: TStringliteral) {
        super.caseTStringliteral(node)
        pushType(node, Type.String)
    }

    override fun caseTTimeliteral(node: TTimeliteral) {
        super.caseTTimeliteral(node)
        pushType(node, Type.Time)
    }

    override fun caseTDigitalpinliteral(node: TDigitalpinliteral) {
        super.caseTDigitalpinliteral(node)
        pushType(node, Type.DigitalPin)
    }

    override fun caseTAnalogpinliteral(node: TAnalogpinliteral) {
        super.caseTAnalogpinliteral(node)
        pushType(node, Type.AnalogPin)
    }

    override fun outAIndexExpr(node: AIndexExpr) {
        val index = typeStack.pop()
        val value = typeStack.pop()

        if (value.isArray())
            if (index.isIntType())
                pushType(node, value.getArraySubType())
            else error(IllegalImplicitTypeConversionError("Indexing must be of type Int, but got $index"))
        else
            error(IllegalImplicitTypeConversionError("Indexing can only be done on type Array, but got $value"))
    }

    override fun outAArrayValue(node: AArrayValue) {
        if (node.expr.size > 0) {
            val type = typeStack.pop()
            for (i in node.expr.drop(1).indices) {
                val ntype = typeStack.pop()
                if (ntype != type) {
                    error(IllegalImplicitTypeConversionError("Last argument indicates array literal of type $type, but argument ${node.expr.size - (i + 1)} was of type $ntype"))
                }
            }
            pushType(node, Type.createArrayOf(type))
        } else
            error(Exception("Array literal was of size 0 (should have been caught in parser)"))
    }

    override fun caseASetToStmt(node: ASetToStmt) {
        val pin = getType(node.pin)

        if (pin.exactlyEquals(Type.AnalogInputPin) || pin.exactlyEquals(Type.DigitalInputPin))
            error(IllegalImplicitTypeConversionError("Cannot set value of input pin."))
        if (!(pin == Type.DigitalPin || pin == Type.AnalogPin))
            error(IllegalImplicitTypeConversionError("Expected type DigitalOutputPin, AnalogOutputPin, DigitalPin or AnalogPin, but got $pin"))

        val value = getType(node.value)
        if (pin == Type.DigitalPin && value != Type.Bool) // Covers all digital pin types (input has already been ruled out)
            error(IllegalImplicitTypeConversionError("Pin was digital so Bool was expected, but instead $value was found"))
        else if (pin == Type.AnalogPin && !value.isIntType())
            error(IllegalImplicitTypeConversionError("Pin was analog so an Int between 0 and 1023 (inclusive) was expected, but $value was found"))

        typeTable[node] = pin
    }

    override fun outAReadExpr(node: AReadExpr) {
        val pin = typeStack.pop()

        if (pin == Type.DigitalInputPin || pin == Type.DigitalPin)
            pushType(node, Type.Bool)
        else if (pin == Type.AnalogInputPin || pin == Type.AnalogPin)
            pushType(node, Type.Int)
        else if (pin == Type.DigitalOutputPin || pin  == Type.AnalogOutputPin)
            error(IllegalImplicitTypeConversionError("Cannot read output pin of type $pin, read can only take DigitalInputPin or AnalogInputPin."))
        else
            error(IllegalImplicitTypeConversionError("Read can only take DigitalInputPin or AnalogInputPin, but got $pin."))
    }

    override fun outADelayStmt(node: ADelayStmt) {
        val t = typeStack.pop()

        if (t != Type.Time)
            throw IllegalImplicitTypeConversionError("Delay expects expression of type Time, but got $t.")
    }

    override fun outADelayuntilStmt(node: ADelayuntilStmt) {
        val t = typeStack.pop()

        if (t != Type.Bool)
            throw IllegalImplicitTypeConversionError("Delay until expects expression of type Bool, but got $t.")
    }

    override fun outASleepStmt(node: ASleepStmt) {
        val t = typeStack.pop()

        if (t != Type.Time)
            error(IllegalImplicitTypeConversionError("Sleep expects expressions of type Time, but got $t"))
    }

    override fun outAUsleepStmt(node: AUsleepStmt) {
        val t = typeStack.pop()

        if (!t.isIntType())
            error(IllegalImplicitTypeConversionError("Usleep expects expressions of type Int, but got $t"))
    }
}