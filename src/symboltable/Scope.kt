package symboltable

import Helper.Companion.lcs

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

    fun nearestMatch(name: String): Pair<String, Int>? {
        val vars = getVars(this)

        return vars.map {Pair(it, lcs(name, it).length)}.maxBy { it.second }
    }

    companion object {
        private fun getVars(scope: Scope): MutableList<String> {
            val v = scope.variables.map {it.key}.toMutableList()
            if (scope.parent != null)
                v.addAll(getVars(scope.parent))
            return v
        }
    }

    override fun toString(): String = "(" + variables.keys.joinToString(", ") + "){" + children.joinToString { it.toString() } + "}"
}