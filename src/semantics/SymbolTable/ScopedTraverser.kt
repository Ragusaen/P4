package semantics.SymbolTable

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

open class ScopedTraverser(protected val symbolTable: SymbolTable) : DepthFirstAdapter() {

    fun traverse(startNode: Start) {
        caseStart(startNode)
    }

    override fun caseStart(node: Start) {
        symbolTable.reset()
        super.caseStart(node)
    }

    override fun inABlockStmt(node: ABlockStmt) = symbolTable.openScope()
    override fun outABlockStmt(node: ABlockStmt) = symbolTable.closeScope()

    override fun inAForStmt(node: AForStmt) = symbolTable.openScope()
    override fun outAForStmt(node: AForStmt) = symbolTable.closeScope()

    override fun inAFunctiondcl(node: AFunctiondcl) = symbolTable.openScope()
    override fun outAFunctiondcl(node: AFunctiondcl) = symbolTable.closeScope()

    override fun inATemplateModuledcl(node: ATemplateModuledcl) = symbolTable.openScope()
    override fun outATemplateModuledcl(node: ATemplateModuledcl) = symbolTable.closeScope()

    override fun inAInstanceModuledcl(node: AInstanceModuledcl) = symbolTable.openScope()
    override fun outAInstanceModuledcl(node: AInstanceModuledcl) = symbolTable.closeScope()
}