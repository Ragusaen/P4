package semantics

import StringLexer
import org.junit.jupiter.api.Test
import java.io.PushbackReader
import sablecc.lexer.Lexer
import sablecc.parser.Parser
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import kotlin.test.assertFailsWith


internal class TypeCheckerTest {
    @Test
    fun typeEqualsTest() {
        val a = Type.INT
        val b = Type.INT

        assertTrue { a == b }
    }

    @Test
    fun assigningFloatToIntThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 5.5;")

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun assigningIntToFloatIsOkay() {
        val (scope, start) = getScopeFromString("Int a = 4; Float b = a;")

        TypeChecker(scope).start(start)

        assert(true)
    }

    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val (scope, start) = getScopeFromString("Int a = 5 + 8;")

        TypeChecker(scope).start(start)

        assert(true)
    }

    @Test
    fun conditionalOperatorIsCompatibleForTwoBooleans() {
        val (scope, start) = getScopeFromString("Bool a = true == false;")

        TypeChecker(scope).start(start)

        assert(true)
    }

    @Test
    fun conditionalOperatorIsCompatibleForTwoInts() {
        val (scope, start) = getScopeFromString("Bool a = 6 == 8;")

        TypeChecker(scope).start(start)

        assert(true)
    }

    @Test
    fun plusAdditionIsIncompatibleForTwoBooleans() {
        val (scope, start) = getScopeFromString("Bool a = true + true;")

        assertThrows<IncompatibleOperatorException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun AdditionBetweenIntAndBoolThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 6 + true;")

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(scope).start(start) }
    }

    fun getScopeFromString(input:String):Pair<Scope, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder().buildSymbolTable(s), s)
    }
}