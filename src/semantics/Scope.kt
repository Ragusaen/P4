package semantics

class Scope(val parent: Scope?) : HashMap<String, Identifier>() {
    val children = mutableListOf<Scope>()
}