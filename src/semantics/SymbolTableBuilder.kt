package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

class SymbolTableBuilder : DepthFirstAdapter() {
    private var currentScope = Scope(null)

    fun add(name:String, identifier: Identifier) {
        // If the name is already used within this scope throw exception
        if (currentScope.contains(name))
            throw IdentifierAlreadyDeclaredException("The variable $name is already declared.")
        else
            currentScope[name] = identifier
    }

    fun checkHasBeenDeclared(name: String) {
        var tempScope:Scope? = currentScope

        while(tempScope != null) {
            if (tempScope.contains(name))
                return
            tempScope = tempScope.parent
        }

        throw IdentifierUsedBeforeDeclarationException("Variable $name was used before it was declared.")
    }

    fun declaredLocally(name:String):Boolean {
        return currentScope.contains(name)
    }

    fun openScope() {
        val newScope = Scope(currentScope)
        currentScope.children.add(newScope)
        currentScope = newScope
    }

    fun closeScope() {
        val parent = currentScope.parent

        if (parent != null)
            currentScope = parent
        else
            throw CloseScopeZeroException("Attempted to close the bottom scope.")
    }

    fun buildSymbolTable(s: Start): Scope {
        caseStart(s)
        return currentScope
    }

/* Tree traversal */
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


    override fun outAVardcl(node: AVardcl) {
        val name = node.identifier.text
        val type = (node.parent() as ADclStmt).type.toString()

        try {
            add(name, Identifier(type, node.expr, node))
        }
        catch (e:IdentifierAlreadyDeclaredException) {
            throw e
            // todo "Append to error list"
        }
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val name = node.identifier.text

        try {
            checkHasBeenDeclared(name)
        }
        catch (e:IdentifierUsedBeforeDeclarationException) {
            throw e
        }
    }
}
