package semantics.TypeChecking

import sablecc.node.*
import semantics.TypeChecking.Exceptions.IllegalImplicitTypeConversionException
import semantics.TypeChecking.Exceptions.IncompatibleOperatorException
import semantics.TypeChecking.Exceptions.IdentifierUsedBeforeAssignmentException
import semantics.SymbolTable.Scope
import semantics.SymbolTable.ScopedTraverser
import java.util.*

class TypeChecker(scope: Scope) : ScopedTraverser(scope) {
    fun start(s: Start) {
        caseStart(s)
    }

    private val typeStack = Stack<Type>()

    private fun convertExpr(from: Type, to: Type, exprNode: PExpr): Boolean {
        if (from == to)
        else if (from == Type.INT && to == Type.FLOAT) {
            val newNode = IntToFloatConversionNode(exprNode)
            exprNode.replaceBy(newNode)
        }
        else
            return false
        return true
    }

    override fun outABinopExpr(node: ABinopExpr) {
        val right = typeStack.pop()
        val left = typeStack.pop()
        val op = node.binop

        val convertedType = when {
            convertExpr(left, right, node.l) -> right
            convertExpr(right, left, node.r) -> left
            else -> throw IllegalImplicitTypeConversionException("Cannot apply binary operations between types $left and $right")
        }

        if(convertedType !in OperatorType.getOperandTypes(node.binop.javaClass.simpleName))
            throw IncompatibleOperatorException("Exception1")

        var newType = Type.BOOL
        if (convertedType in OperatorType.getReturnTypes(node.binop.javaClass.simpleName))
            newType = convertedType
        else if (newType !in OperatorType.getReturnTypes(node.binop.javaClass.simpleName))
            throw IncompatibleOperatorException("Exception")

        typeStack.push(newType)
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = scope.findVar(node.identifier.text)!!
        val expr = node.expr

        if (expr != null) {
            val typeE = typeStack.pop()
            identifier.isInitialised = true
            if (!convertExpr(typeE, identifier.type, expr))
                throw IllegalImplicitTypeConversionException("Cannot initialise variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE.")
        }
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
        val typeId = scope.findVar(node.identifier.text)!!.type

        if (!convertExpr(typeExpr, typeId, node.expr)){
            throw IllegalImplicitTypeConversionException("Cannot assign variable ${node.identifier.text} of type $typeId with value of type $typeExpr.")
        }
        scope.findVar(node.identifier.text)!!.isInitialised = true
    }

    // This is only for variables used in expressions as values
    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = scope.findVar(node.identifier.text)
        typeStack.push(identifier!!.type)
        if (!identifier.isInitialised)
            throw IdentifierUsedBeforeAssignmentException("The variable ${node.identifier.text} was used before being initialised.")
    }

    override fun caseTIntliteral(node: TIntliteral) {
        typeStack.push(Type.INT)
    }

    override fun caseTFloatliteral(node: TFloatliteral?) {
        typeStack.push(Type.FLOAT)
    }

    override fun caseTBoolliteral(node: TBoolliteral?) {
        typeStack.push(Type.BOOL)
    }

    override fun caseTStringliteral(node: TStringliteral?) {
        typeStack.push(Type.STRING)
    }
}