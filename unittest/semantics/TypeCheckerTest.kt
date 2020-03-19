package semantics

import org.junit.jupiter.api.Test
import java.io.PushbackReader
import sablecc.lexer.Lexer
import sablecc.parser.Parser
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows


internal class TypeCheckerTest {
    @Test
    fun typeEqualsTest(){
        val a = Type("Int")
        val b = Type("Int")

        assertTrue { a == b }
    }

    @Test
    fun assigmentOfFloatExprToIntIdentifierThrowsError() {
        val input = "a = 5.5;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val s = parser.parse()

        val st = SymbolTableBuilder().buildSymbolTable(s)
        TypeChecker(st).start(s)

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(st).start(s) }
    }

    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val input = "Int a = 5 + 8;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val a = parser.parse()
    }

    @Test
    fun assigningFloatToIntVarThrowsError(){
        val input = "Int a = 4.5;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val a = parser.parse()


    }
}