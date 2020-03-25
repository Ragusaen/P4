package semantics.SymbolTable

import semantics.TypeChecking.Type


/*
    Functions should be looked up with the findFun method, but since variables can be in different scopes,
    they should not be directly looked up in the symbol table. For that use the ScopedTraverser.
 */
class SymbolTable(functionTable : SymbolTableBuilder.FunctionTable, var variables: Scope) {
    private val functions = functionTable.table

    fun findFun(name: String, paramTypes: List<Type>): Identifier? = functions[Pair(name, paramTypes)]

    fun findVar(name: String): Identifier? = variables.findVar(name)


    private val childN = mutableListOf(0)

    fun openScope() {
        variables = variables.children[childN.last()]
        childN[childN.lastIndex]++
        childN.add(childN.size, 0)
    }

    fun closeScope() {
        variables = variables.parent!!
        childN.removeAt(childN.lastIndex)
    }
}