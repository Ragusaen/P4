package semantics.TypeChecking

import CompileError
import ErrorHandler
import sablecc.node.*
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable
import semantics.TypeChecking.errors.*
import java.util.*

class TypeChecker(symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {
    fun run(node: Start): MutableMap<Node, Type> {
        caseStart(node)
        return typeTable
    }

    // Error handling
    private val errorHandler = ErrorHandler()
    private fun error(ce:CompileError):Nothing = errorHandler.compileError(ce)

    private val typeStack = Stack<Type>()

    private val typeTable = mutableMapOf<Node, Type>()

    private fun pushType(node: Node, type: Type) {
        typeStack.push(type)
        typeTable[node] = type
    }

    private var currentFunctionReturnType: Type? = null
    private var currentFunctionName:String? = null

    override fun outAReturnStmt(node: AReturnStmt) {
        val type = typeStack.pop()
        if (currentFunctionReturnType != type) {
            error(IllegalImplicitTypeConversionError(
                    "In function $currentFunctionName:\n" +
                    "Expected return statement of type $currentFunctionReturnType, but got $type."))
        }
    }

    override fun outAModuledclStmt(node: AModuledclStmt) {
        val name = node.instance.text
        // Pop the expressions from the typeStack
        val types = mutableListOf<Type>()
        for (i in 0 until node.expr.size)
            types.add(typeStack.pop())

        // The expressions are popped in reverse order
        types.reverse()

        val id = symbolTable.findTemplateModule(name) ?: error(IdentifierNotDeclaredError("Module with name $name does not exist"))

        if (id.paramTypes != types)
            error(IllegalImplicitTypeConversionError("Module $name expects types ${id.paramTypes}, but got $types"))
    }

    override fun outAEveryModuleStructure(node: AEveryModuleStructure) {
        super.outAEveryModuleStructure(node)
        val conditionType = typeStack.pop()

        if (conditionType != Type.Time)
            error(IllegalImplicitTypeConversionError("'Every' expects expression of type Time, but got $conditionType"))
    }

    override fun outAIfStmt(node: AIfStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.Bool)
            error(IllegalImplicitTypeConversionError("'If' expects expression of type Bool, but got $conditionType"))
    }

    override fun outAForStmt(node: AForStmt) {
        super.outAForStmt(node)
        val conditionType = typeStack.pop()

        if (conditionType != Type.Bool)
            error(IllegalImplicitTypeConversionError("'For' expects middle expression of type Bool, but got $conditionType"))
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
        errorHandler.setLineAndPos(node.identifier)
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
                error(ArrayInitializationError("Cannot declare array ${node.identifier.text} with no size parameters"))
            }
        }

        if (node.expr != null) {
            val typeE = typeStack.pop()
            if (identifier.type.isPin()) {
                if (typeE != identifier.type) {
                    if ((identifier.type == Type.AnalogOutputPin || identifier.type == Type.AnalogInputPin) && typeE != Type.AnalogPin)
                        error(IllegalImplicitTypeConversionError("Cannot assign type $typeE to an analog pin."))
                    else if ((identifier.type == Type.DigitalOutputPin || identifier.type == Type.DigitalInputPin) && typeE != Type.DigitalPin)
                        error(IllegalImplicitTypeConversionError("Cannot assign type $typeE to a digital pin."))
                }
            }
            else if (typeE != identifier.type)
                error(IllegalImplicitTypeConversionError("Cannot initialize the variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE."))
        }
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        super.inAFunctiondcl(node)

        currentFunctionReturnType = symbolTable.findFun(node).type
        currentFunctionName = node.identifier.text
    }

    override fun outAUnopExpr(node: AUnopExpr) {
        val exprType = typeStack.peek()

        when(node.unop) {
            is ANotUnop -> if (exprType != Type.Bool)
                error(IncompatibleOperatorError("Cannot apply conditional unary '!' operator to expression of $exprType"))
            is APlusUnop -> if (exprType != Type.Int && exprType != Type.Float)
                error(IncompatibleOperatorError("Cannot apply conditional unary '+' operator to expression of $exprType"))
            is AMinusUnop -> if (exprType != Type.Int && exprType != Type.Float)
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
        errorHandler.setLineAndPos(node.identifier)
        val identifier = symbolTable.findVar(node.identifier.text)
        pushType(node, identifier!!.type)
    }

    override fun outAValueExpr(node: AValueExpr) {
        pushType(node, typeStack.pop())
    }

    override fun caseTIntliteral(node: TIntliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.Int)
    }

    override fun caseTFloatliteral(node: TFloatliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.Float)
    }

    override fun caseTBoolliteral(node: TBoolliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.Bool)
    }

    override fun caseTStringliteral(node: TStringliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.String)
    }

    override fun caseTTimeliteral(node: TTimeliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.Time)
    }

    override fun caseTDigitalpinliteral(node: TDigitalpinliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.DigitalPin)
    }

    override fun caseTAnalogpinliteral(node: TAnalogpinliteral) {
        errorHandler.setLineAndPos(node)
        pushType(node, Type.AnalogPin)
    }

    override fun outAIndexExpr(node: AIndexExpr) {
        val index = typeStack.pop()
        val value = typeStack.pop()

        if (value.isArray())
            if (index == Type.Int)
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

    override fun outASetToStmt(node: ASetToStmt) {
        val value = typeStack.pop()
        val pin = typeStack.pop()

        if ((pin == Type.DigitalOutputPin || pin == Type.DigitalPin) && value != Type.Bool)
            error(IllegalImplicitTypeConversionError("Pin was digital so Bool was expected, but instead $value was found"))
        else if ((pin == Type.AnalogOutputPin || pin == Type.AnalogPin) && value != Type.Int)
            error(IllegalImplicitTypeConversionError("Pin was analog so an Int between 0 and 1023 (inclusive) was expected, but $value was found"))
        else if (pin == Type.AnalogInputPin || pin == Type.DigitalInputPin)
            error(IllegalImplicitTypeConversionError("Cannot set value of input pin."))
        else if (!(pin == Type.DigitalOutputPin || pin == Type.AnalogOutputPin || pin == Type.DigitalPin || pin == Type.AnalogPin))
            error(IllegalImplicitTypeConversionError("Expected type DigitalOutputPin, AnalogOutputPin, DigitalPin or AnalogPin, but got $pin"))

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
}