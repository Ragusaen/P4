package semantics

import PrettyPrinter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reader
import sablecc.lexer.Lexer
import java.lang.Error
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import sablecc.node.*
import sablecc.parser.Parser


internal class TypeCheckerTest {
    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val input = "5+8"
        val lexer = Lexer(reader(input.toCharArray()))
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
        val lexer = Lexer(reader(input.toCharArray()))
        val parser = Parser(lexer)

        val a = parser.parse()

        PrettyPrinter().print(a)
    }
}