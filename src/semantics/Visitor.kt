package semantics

import sablecc.analysis.Analysis
import sablecc.analysis.DepthFirstAdapter
import sablecc.node.AAdditionBinop
import sablecc.node.Node
import sablecc.node.Switch

open class Visitor : DepthFirstAdapter() {
    fun visit(node:Node) {
        node.apply(this)
    }

    override fun caseAAdditionBinop(node: AAdditionBinop?) {
        super.caseAAdditionBinop(node)
    }

    override fun defaultIn(node: Node?) {
        super.defaultIn(node)
    }

    override fun defaultOut(node: Node?) {
        super.defaultOut(node)
    }
}

class TypeChecking : Visitor() {
    override fun toString() = "Not yet implemented"
}