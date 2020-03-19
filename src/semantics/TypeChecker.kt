package semantics

import sablecc.node.*
import java.util.*

class TypeChecker(scope:Scope) : ScopedTraverser(scope) {
    fun start(s: Start) {
        caseStart(s)
    }

    val ts = Stack<Type>()

    fun convertExpr(from:Type, to:Type, exprNode:PExpr):Boolean {
        if (from == to)
            // Types are already matching, no action required
        else if (from == Type.INT && to == Type.FLOAT) {
            val newNode = IntToFloatConversionNode(exprNode)
            exprNode.replaceBy(newNode)
        }
        else
            return false
        return true
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = scope.find(node.identifier.text)!!
        val expr = node.expr

        if (expr != null) {
            val typeE = ts.pop()
            identifier.isInitialised = true
            if (convertExpr(typeE, identifier.type, expr))
                throw IllegalImplicitTypeConversionException("Cannot initialise variable ${node.identifier.text} of type ${identifier.type} with value of type $typeE.")
        }
    }

    override fun outABinopExpr(node: ABinopExpr) {
        val right = ts.pop()
        val left = ts.pop()
        if (!convertExpr(left, right, node.l) && !convertExpr(right, left, node.r))
            throw IllegalImplicitTypeConversionException("Cannot apply binary operations between types $left and $right")
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        val typeExpr = ts.pop()
        val typeId = ts.pop()
        convertExpr(typeExpr, typeId, node.expr)
        scope.find(node.identifier.text)!!.isInitialised = true
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = scope.find(node.identifier.text)
        ts.push(identifier!!.type)
        if (!identifier.isInitialised)
            throw IdentifierUsedBeforeAssignmentException("The variable ${node.identifier.text} was used before being initialised.")
    }

    override fun caseTIntliteral(node: TIntliteral) {
        ts.push(Type.INT)
    }

    override fun caseTFloatliteral(node: TFloatliteral?) {
        ts.push(Type.FLOAT)
    }

    override fun caseTBoolliteral(node: TBoolliteral?) {
        ts.push(Type.BOOL)
    }

    override fun caseTStringliteral(node: TStringliteral?) {
        ts.push(Type.STRING)
    }
}