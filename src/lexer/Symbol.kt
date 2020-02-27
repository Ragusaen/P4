package lexer

data class Symbol @JvmOverloads constructor(val type: Int, val line: Int, val column: Int, val value: Any? = null) {

}

enum class SymType {
    STRING,

    EVERY,
}
