package semantics.SymbolTable

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

open class ScopedTraverser(protected val symbolTable: SymbolTable) : DepthFirstAdapter() {

    fun traverse(startNode: Start) {
        caseStart(startNode)
    }

    override fun inABlockStmt(node: ABlockStmt) = symbolTable.openScope()
    override fun outABlockStmt(node: ABlockStmt) = symbolTable.closeScope()

    override fun inAForStmt(node: AForStmt) = symbolTable.openScope()
    override fun outAForStmt(node: AForStmt) = symbolTable.closeScope()

    override fun inATemplateModuledcl(node: ATemplateModuledcl) = symbolTable.openScope()
    override fun outATemplateModuledcl(node: ATemplateModuledcl) = symbolTable.closeScope()

    override fun inAInstanceModuledcl(node: AInstanceModuledcl) = symbolTable.openScope()
    override fun outAInstanceModuledcl(node: AInstanceModuledcl) = symbolTable.closeScope()
}