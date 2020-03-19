package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import java.util.*
import kotlin.reflect.typeOf

class TypeChecker(private val st:SymbolTable) : DepthFirstAdapter() {
    fun start(s: Start) {
        caseStart(s)
    }

    val ts = Stack<Type>()


    override fun inAAssignStmt(node: AAssignStmt) {
        val TypeI:Type = getIn(node.identifier) as Type
        val TypeE:Type = getIn(node.expr) as Type
        if (TypeI.ImplicitCastAllowed(TypeE))
            setIn(node, Type(TypeI))
        else
            throw IllegalImplicitTypeConversionException("Type $TypeE cannot be assigned to variable of type $TypeI")
    }

    override fun outAIdentiierValue(node: AIdentifierValue) {
        ts.push()
    }

    override fun caseTIntliteral(node: TIntliteral) {

    }

    override fun inABinopExpr(node: ABinopExpr) {

    }
}