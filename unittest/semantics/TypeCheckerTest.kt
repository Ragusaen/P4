package semantics

import StringLexer
import org.junit.jupiter.api.Test
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.SymbolTable.Scope
import semantics.SymbolTable.SymbolTable
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.Exceptions.IllegalImplicitTypeConversionException
import semantics.TypeChecking.Exceptions.IncompatibleOperatorException
import semantics.TypeChecking.TypeChecker


internal class TypeCheckerTest {
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
    fun additionBetweenIntAndBoolThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 6 + true;")

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun parenthesisOnIntExpressionYieldsIntExpression() {
        val (scope, start) = getScopeFromString("Int a = (5 + 3);")

        TypeChecker(scope).start(start)
    }

    @Test
    fun chainedGreaterThanOperationsThrowsConversionException() {
        val (scope, start) = getScopeFromString("Bool a = 1 < 2 < 3;")

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(scope).start(start)}
    }

    @Test
    fun chainedEqualsOperationsYieldsBoolExpression() {
        val (scope, start) = getScopeFromString("Bool a = 1 == 2 == (2 == 2);")

        TypeChecker(scope).start(start)
    }

    @Test
    fun comparisonBetweenFloatAndIntConvertsToFloat() {
        val (st, start) = getScopeFromString("Bool a = 6 == 6.6 or 7.2 == 7;")

        TypeChecker(st).start(start)
    }

    @Test
    fun valueOfFunctionReturningIntCannotBeAssignedToBool() {
        val (st, start) = getScopeFromString("fun foo(): Int {;} Bool a = foo();")

        assertThrows<IllegalImplicitTypeConversionException> {TypeChecker(st).start(start)}
    }

    fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder().buildSymbolTable(s), s)
    }
}