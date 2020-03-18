package semantics

import PrettyPrinter
import org.junit.jupiter.api.Test
import java.io.PushbackReader
import sablecc.lexer.Lexer
import sablecc.parser.Parser


internal class TypeCheckerTest {
    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val input = "5+8"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val a = parser.parse()

        PrettyPrinter().print(a)
    }

    @Test
    @Suppress("UNREACHABLE_CODE")
    fun plusAdditionIsTypeIncorrectForIntegerAndBoolean(){
        TODO("Boolean cfg not implemented.")

        return
        val input = "5+false"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val a = parser.parse()

        PrettyPrinter().print(a)
    }
}