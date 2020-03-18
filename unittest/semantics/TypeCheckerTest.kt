package semantics

import PrettyPrinter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.lexer.Lexer
import java.lang.Error
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import sablecc.node.*
import sablecc.parser.Parser
import java.io.PushbackReader
import java.io.Reader


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