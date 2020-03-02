import lexer.*
import java.io.File

fun main() {

    val reader = File("/home/kasper/P4/build/production/P4/test.txt").reader()
    val lexer = DumplingLexer(reader)


    val symbolList = mutableListOf<Symbol>()

    while (!lexer.yyatEOF()) println(lexer.yylex())

}

