package semantics

class SymbolTable {
    var depth:Int = 0
    var currentScope = Scope(null)

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

    fun empty() {

    }

    fun declaredLocally(name:String) {

    }

    fun OpenScope() {
        depth++
    }

    fun closeScope() {
        depth--
        // Delete identifiers in hashtable with top-level depth
    }
}