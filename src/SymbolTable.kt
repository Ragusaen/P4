class SymbolTable {
    val HashTable:HashMap<String, Identifier> = hashMapOf<String, Identifier>()

    fun OpenScope() {

    }

    fun closeScope() {

    }
}

class SubSymbolTable {
    fun add(symbol: Identifier) {

    }

    fun find(name: String) {

    }

    fun empty() {

    }

    fun declaredLocally(name:String) {

    }
}