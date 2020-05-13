package semantics.symbolTable

import sablecc.node.Node
import semantics.typeChecking.Type


/*
    Functions should be looked up with the findFun method, but since variables can be in different scopes,
    they should not be directly looked up in the symbol table. For that use the ScopedTraverser.
 */
class SymbolTable(private val namedFunctions: Map<Pair<String, List<Type>>, Identifier>,
                  private val nodeFunctions: Map<Node, Identifier>,
                  private var variables: Scope,
                  private val templateModules: Map<String, TemplateModuleIdentifier>,
                  private val moduleTable: Map<String, String>,
                  private val nodeModules: Map<Node, String>
) {


    fun findVar(name: String): Identifier? = variables.findVar(name)

    fun findFun(name: String, paramTypes: List<Type>): Identifier? = namedFunctions[Pair(name, paramTypes)]
    fun findFun(node: Node): Identifier = nodeFunctions[node]!!

    fun findTemplateModule(name: String): TemplateModuleIdentifier? = templateModules[name]

    fun findModule(node: Node): Pair<String, String>?  {
        val name = nodeModules[node]
        val template = moduleTable[name]
        if (name != null && template != null)
            return Pair(name, template)
        else
            return null
    }


    private val childN = mutableListOf(0)

    fun reset(): SymbolTable {
        childN.clear()
        childN.add(0)
        return this
    }

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