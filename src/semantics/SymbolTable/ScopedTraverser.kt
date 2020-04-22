package semantics.SymbolTable

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.ABlockStmt
import sablecc.node.AForStmt
import sablecc.node.AInnerModule
import sablecc.node.Start

open class ScopedTraverser(protected val symbolTable: SymbolTable) : DepthFirstAdapter() {

    fun traverse(startNode: Start) {
        caseStart(startNode)
    }

    override fun inABlockStmt(node: ABlockStmt) = symbolTable.openScope()
    override fun outABlockStmt(node: ABlockStmt) = symbolTable.closeScope()

    override fun inAForStmt(node: AForStmt) = symbolTable.openScope()
    override fun outAForStmt(node: AForStmt) = symbolTable.closeScope()

    override fun inAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            symbolTable.openScope()
        }
    }
    override fun outAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            symbolTable.closeScope()
        }
    }

}