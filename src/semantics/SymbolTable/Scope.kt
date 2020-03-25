package semantics.SymbolTable

import semantics.SymbolTable.Exceptions.FunctionIdentifierUsedAsVariable
import semantics.SymbolTable.Exceptions.VariableIdentifierUsedAsFunction

class Scope(val parent: Scope?) : HashMap<String, Identifier>() {
    val children = mutableListOf<Scope>()

    fun findVar(name: String): Identifier? {
        var tempScope: Scope? = this

        while(tempScope != null) {
            if (tempScope.contains(name)) {
                return tempScope[name]!!
            }
            tempScope = tempScope.parent
        }

        return null
    }
}