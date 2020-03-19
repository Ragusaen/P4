package semantics

import org.junit.jupiter.api.Test
import java.io.PushbackReader
import sablecc.lexer.Lexer
import sablecc.parser.Parser
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFailsWith


internal class TypeCheckerTest {
    @Test
    fun typeEqualsTest(){
        val a = Type.INT
        val b = Type.INT

        assertTrue { a == b }
    }

    @Test
    fun assigningFloatToIntThrowsException() {
        val input = "Int a = 5.5;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val s = parser.parse()
        val st = SymbolTableBuilder().buildSymbolTable(s)

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(st).start(s) }
    }

    @Test
    fun assigningIntToFloatIsOkay(){
        val input = "Int a = 4; Float b = a;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val s = parser.parse()
        val scope = SymbolTableBuilder().buildSymbolTable(s)

        TypeChecker(scope).start(s)

        assert(true)
    }

    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val input = "Int a = 5 + 8;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val s = parser.parse()
        val scope = SymbolTableBuilder().buildSymbolTable(s)

        TypeChecker(scope).start(s)

        assert(true)
    }
}