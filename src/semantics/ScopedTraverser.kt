package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.ABlockStmt
import sablecc.node.AForStmt
import sablecc.node.AInnerModule
import sablecc.node.Start

open class ScopedTraverser(protected var scope: Scope) : DepthFirstAdapter() {
    val childN = mutableListOf<Int>(0)

    fun traverse(startNode: Start) {
        caseStart(startNode)
    }

    private fun openScope() {
        scope = scope.children[childN.last()]
        childN[childN.lastIndex]++
        childN.add(childN.size, 0)
    }

    private fun closeScope() {
        scope = scope.parent!!
        childN.removeAt(childN.lastIndex)
    }

    override fun inABlockStmt(node: ABlockStmt) = openScope()
    override fun outABlockStmt(node: ABlockStmt) = closeScope()

    override fun inAForStmt(node: AForStmt) = openScope()
    override fun outAForStmt(node: AForStmt) = closeScope()

    override fun inAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            openScope()
        }
    }
    override fun outAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            closeScope()
        }
    }

}