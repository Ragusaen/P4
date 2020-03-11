package semantics

import semantics.Identifier

class SymbolTable {
    val HashTable = hashMapOf<String, MutableList<Identifier>>()
    var depth:Int = 0


    fun add(name:String, identifier: Identifier) {
        // Checks whether the hashtable contains the identifier already, if so append the identifier,
        // if not a new identifier accompanied with a list is added to the hashtable.
        HashTable[name]?.add(identifier) ?: HashTable.put(name, mutableListOf(identifier))

    }

    fun find(name: String) {
        var a = HashTable[name]
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