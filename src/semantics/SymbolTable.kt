package semantics

class SymbolTable {
    private var currentScope = Scope(null)

    fun add(name:String, identifier: Identifier) {
        // If the name is already used within this scope throw exception
        if (currentScope.contains(name))
            throw IdentifierAlreadyDeclaredException("The variable $name is already declared.")
        else
            currentScope[name] = identifier
    }

    fun find(name: String):Identifier {
        var tempScope:Scope? = currentScope

        while(tempScope != null) {
            if (tempScope.contains(name))
                return tempScope[name]!!
            tempScope = tempScope.parent
        }

        throw IdentifierUsedBeforeDeclarationException("Variable $name was used before it was declared.")
    }

    fun declaredLocally(name:String) {

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
}