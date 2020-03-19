package semantics

class Scope(val parent: Scope?) : HashMap<String, Identifier>() {
    val children = mutableListOf<Scope>()

    fun find(name: String): Identifier? {
        var tempScope: Scope? = this

        while(tempScope != null) {
            if (tempScope.contains(name))
                return tempScope[name]!!
            tempScope = tempScope.parent
        }

        return null
    }
}