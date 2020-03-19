package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import java.util.*
import kotlin.reflect.typeOf

class TypeChecker(private val scope:Scope) : DepthFirstAdapter() {
    fun start(s: Start) {
        caseStart(s)
    }

    val ts = Stack<Type>()

    fun getCombinedType(left:Type, right:Type):Type? {
        if (left == right)
            return left
        if(left == Type.INT && left == Type.FLOAT)
            return Type.FLOAT
        if (left == Type.FLOAT && right == Type.INT)
            return Type.FLOAT

        return null
    }

    /*Tree traversal*/
    override fun inAAssignStmt(node: AAssignStmt) {
        val typeExpr = ts.pop()
        val typeId = ts.pop()


    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = scope.find(node.identifier.text)
        if (identifier == null)
            throw IdentifierUsedBeforeDeclarationException("The variable $identifier was used before being declared.")
        else
            ts.push(identifier.type)
    }

    override fun caseTIntliteral(node: TIntliteral) {

    }

    override fun inABinopExpr(node: ABinopExpr) {

    }
}