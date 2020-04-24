package semantics.SymbolTable

import semantics.SymbolTable.Exceptions.FunctionIdentifierUsedAsVariable
import semantics.SymbolTable.Exceptions.VariableIdentifierUsedAsFunction

class Scope(val parent: Scope?) {
    val children = mutableListOf<Scope>()

    val variables = HashMap<String, Identifier>()

    fun findVar(name: String): Identifier? {
        var tempScope: Scope? = this

        while(tempScope != null) {
            if (tempScope.variables.contains(name)) {
                return tempScope.variables[name]!!
            }
            tempScope = tempScope.parent
        }

        return null
    }

    override fun toString(): String = "(" + variables.keys.joinToString(", ") + "){" + children.joinToString { it.toString() } + "}"
}