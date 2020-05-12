package semantics.symbolTable

class Scope(val parent: Scope?) {
    val children = mutableListOf<Scope>()
    val variables = HashMap<String, Identifier>()

    fun findVar(name: String): Identifier? {
        if (this.variables.contains(name))
            return this.variables[name]!!
        else if(parent != null)
            return parent.findVar(name)

        return null
    }

    override fun toString(): String = "(" + variables.keys.joinToString(", ") + "){" + children.joinToString { it.toString() } + "}"
}