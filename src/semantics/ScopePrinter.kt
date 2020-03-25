package semantics

import sablecc.node.ABlockStmt
import sablecc.node.AInnerModule
import sablecc.node.AProgram
import semantics.SymbolTable.Scope
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable

class ScopePrinter(symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {

    override fun inAProgram(node: AProgram?) {
        super.inAProgram(node)
        println(symbolTable.variables)
    }

    override fun inABlockStmt(node: ABlockStmt) {
        super.inABlockStmt(node)
        println(symbolTable.variables)
    }

    override fun inAInnerModule(node: AInnerModule) {
        super.inAInnerModule(node)
        println(symbolTable.variables)
    }
}