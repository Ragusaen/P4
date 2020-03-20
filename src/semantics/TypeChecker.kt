package semantics

import sablecc.node.*
import java.util.*

class TypeChecker(scope:Scope) : ScopedTraverser(scope) {
    fun start(s: Start) {
        caseStart(s)
    }

    private val typeStack = Stack<Type>()

    private fun convertExpr(from: Type, to: Type, exprNode: PExpr): Type? {
        if (from == to) {
            return from
        }
        else if (from == Type.INT && to == Type.FLOAT) {
            val newNode = IntToFloatConversionNode(exprNode)
            exprNode.replaceBy(newNode)
            return Type.FLOAT
        }
        else
            return null
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = scope.find(node.identifier.text)!!
        val expr = node.expr

        if (expr != null) {
            val typeE = typeStack.pop()
            identifier.isInitialised = true
            if (convertExpr(typeE, identifier.type, expr) == null)
                throw IllegalImplicitTypeConversionException("Cannot initialise variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE.")
        }
    }

    override fun outABinopExpr(node: ABinopExpr) {
        val right = typeStack.pop()
        val left = typeStack.pop()

        var newType = convertExpr(left, right, node.l)
        if (newType == null)
            newType = convertExpr(right, left, node.r)

        if (newType == null)
            throw IllegalImplicitTypeConversionException("Cannot apply binary operations between types $left and $right")

        typeStack.push(newType)
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        val typeExpr = typeStack.pop()
        val typeId = scope.find(node.identifier.text)!!.type

        if (convertExpr(typeExpr, typeId, node.expr) == null){
            throw IllegalImplicitTypeConversionException("Cannot assign variable ${node.identifier.text} of type $typeId with value of type $typeExpr.")
        }
        scope.find(node.identifier.text)!!.isInitialised = true
    }

    // This is only for variables used in expressions as values
    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = scope.find(node.identifier.text)
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