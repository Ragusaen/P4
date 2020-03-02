import lexer.*
import java.nio.file.Paths

fun main() {
    val reader = Paths.get("", "resources", "test.txt").toFile().reader()
    val lexer = DumplingLexer(reader)


    val tokenList = mutableListOf<Symbol>()

    while (!lexer.yyatEOF()) {
        val p = lexer.yylex()
        if (p != null) {
            tokenList.add(p)
        }
    }

    tokenList.removeAt(tokenList.lastIndex)
    println(tokenList.map{it.type.name}.joinToString(" "))
}

