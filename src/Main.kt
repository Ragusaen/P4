import lexer.*
import java.nio.file.Paths

fun main() {
    val reader = Paths.get("", "resources", "test.txt").toFile().reader()
    val lexer = DumplingLexer(reader)


    val tokenList = mutableListOf<Symbol>()

    while (!lexer.yyatEOF()) tokenList.add(lexer.yylex() ?: Symbol(SymType.EOF, 0, 0))

    println(tokenList.map{it.type.name}.joinToString(""))
}

