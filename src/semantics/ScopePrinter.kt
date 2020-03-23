package semantics

import sablecc.node.ABlockStmt
import sablecc.node.AInnerModule
import sablecc.node.AProgram
import semantics.SymbolTable.Scope
import semantics.SymbolTable.ScopedTraverser

class ScopePrinter(scope: Scope) : ScopedTraverser(scope) {

    override fun inAProgram(node: AProgram?) {
        super.inAProgram(node)
        println(scope)
    }

    override fun inABlockStmt(node: ABlockStmt) {
        super.inABlockStmt(node)
        println(scope)
    }

    override fun inAInnerModule(node: AInnerModule) {
        super.inAInnerModule(node)
        println(scope)
    }
}