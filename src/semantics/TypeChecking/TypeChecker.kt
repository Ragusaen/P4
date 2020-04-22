package semantics.TypeChecking

import sablecc.node.*
import semantics.TypeChecking.Exceptions.IllegalImplicitTypeConversionException
import semantics.TypeChecking.Exceptions.IncompatibleOperatorException
import semantics.TypeChecking.Exceptions.IdentifierUsedBeforeAssignmentException
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable
import semantics.TypeChecking.Exceptions.IdentifierNotDeclaredException
import java.util.*

class TypeChecker(symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {
    fun start(s: Start) {
        caseStart(s)
    }

    private val typeStack = Stack<Type>()

    private val typeTable = mutableMapOf<Node, Type>()

    private fun pushType(node: Node, type: Type) {
        typeStack.push(type)
        typeTable[node] = type
    }

    fun run(node: Start): MutableMap<Node, Type> {
        caseStart(node)
        return typeTable
    }

    private var currentFunctionReturnType: Type? = null

    /*
    private fun convertExpr(from: Type, to: Type, exprNode: PExpr): Boolean {
        if (from == to)
        else if (from == Type.INT && to == Type.FLOAT) {
            val newNode = IntToFloatConversionNode(exprNode)
            exprNode.replaceBy(newNode)
        }
        else
            return false
        return true
    }*/

    override fun outAReturnStmt(node: AReturnStmt) {
        val type = typeStack.pop()
        if (currentFunctionReturnType!! != type) {
            throw IllegalImplicitTypeConversionException("Expected function to return $currentFunctionReturnType but got $type")
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

        val id = symbolTable.findModule(name)
                ?: throw IdentifierNotDeclaredException("Module with name $name does not exist")

        if (id.paramTypes != types)
            throw IllegalImplicitTypeConversionException("Module $name expects types ${id.paramTypes}, but got $types")
    }

    override fun outAEveryModuleStructure(node: AEveryModuleStructure) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.TIME)
            throw IllegalImplicitTypeConversionException("'Every' expects expression of type Time, but got $conditionType")
    }

    override fun outAIfStmt(node: AIfStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.BOOL)
            throw IllegalImplicitTypeConversionException("'If' expects expression of type Bool, but got $conditionType")
    }

    override fun outAForStmt(node: AForStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.BOOL)
            throw IllegalImplicitTypeConversionException("'For' expects middle expression of type Bool, but got $conditionType")
    }

    override fun outAWhileStmt(node: AWhileStmt) {
        val conditionType = typeStack.pop()

        if (conditionType != Type.BOOL)
            throw IllegalImplicitTypeConversionException("'While' expects expression of type Bool, but got $conditionType")
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
        else
            throw IdentifierNotDeclaredException("Function with name $name and parameter types ${types.joinToString (", ")} does not exist")
    }


    override fun outABinopExpr(node: ABinopExpr) {
        val rType = typeStack.pop()
        val lType = typeStack.pop()
        val op = node.binop

        if (lType != rType)
            throw IllegalImplicitTypeConversionException("Cannot apply binary operations between types $lType and $rType")

        val operandType = lType

        if(operandType !in OperatorType.getOperandTypes(node.binop.javaClass.simpleName))
            throw IncompatibleOperatorException("Operator $op cannot take operands of type $lType")

        var returnType = Type.BOOL
        if (operandType in OperatorType.getReturnTypes(node.binop.javaClass.simpleName))
            returnType = operandType
        else if (returnType !in OperatorType.getReturnTypes(node.binop.javaClass.simpleName))
            throw IncompatibleOperatorException("Invalid return type")

        pushType(node, returnType)
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = symbolTable.findVar(node.identifier.text)!!
        val expr = node.expr

        if (expr != null) {
            val typeE = typeStack.pop()
            identifier.isInitialised = true
            if (typeE != identifier.type)
                throw IllegalImplicitTypeConversionException("Cannot initialise variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE.")
        }
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        currentFunctionReturnType = symbolTable.findFun(node).type
    }

    override fun outAUnopExpr(node: AUnopExpr) {
        val exprType = typeStack.peek()

        when(node.unop) {
            is ANotUnop -> if (exprType != Type.BOOL)
                throw IncompatibleOperatorException("Cannot apply conditional unary '!' operator to expression of $exprType")
            is APlusUnop -> if (exprType != Type.INT && exprType != Type.FLOAT)
                throw IncompatibleOperatorException("Cannot apply conditional unary '+' operator to expression of $exprType")
            is AMinusUnop -> if (exprType != Type.INT && exprType != Type.FLOAT)
                throw IncompatibleOperatorException("Cannot apply conditional unary '-' operator to expression of $exprType")
        }
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        val typeExpr = typeStack.pop()
        val typeId = symbolTable.findVar(node.identifier.text)!!.type

        if (typeExpr != typeId){
            throw IllegalImplicitTypeConversionException("Cannot assign variable ${node.identifier.text} of type $typeId with value of type $typeExpr.")
        }
        symbolTable.findVar(node.identifier.text)!!.isInitialised = true
    }

    // This is only for variables used in expressions as values
    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = symbolTable.findVar(node.identifier.text)
        pushType(node, identifier!!.type)
        if (!identifier.isInitialised)
            throw IdentifierUsedBeforeAssignmentException("The variable ${node.identifier.text} was used before being initialised.")
    }

    override fun outAValueExpr(node: AValueExpr) {
        pushType(node, typeStack.pop())
    }

    override fun caseTIntliteral(node: TIntliteral) {
        pushType(node, Type.INT)
    }

    override fun caseTFloatliteral(node: TFloatliteral) {
        pushType(node, Type.FLOAT)
    }

    override fun caseTBoolliteral(node: TBoolliteral) {
        pushType(node, Type.BOOL)
    }

    override fun caseTStringliteral(node: TStringliteral) {
        pushType(node, Type.STRING)
    }

    override fun caseTTimeliteral(node: TTimeliteral) {
        pushType(node, Type.TIME)
    }
}