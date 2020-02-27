import lexer.DumplingLexer
import lexer.Symbol
import java.io.StringReader

fun main() {
    val reader = StringReader("every \"this is string\" every")

    val lexer = DumplingLexer(reader)

    val symbolList = mutableListOf<Symbol>()

    while (!lexer.yyatEOF()) println(lexer.yylex())

}