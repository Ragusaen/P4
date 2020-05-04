package semantics

import StringLexer
import org.junit.jupiter.api.Test
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.SymbolTable.SymbolTable
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.Exceptions.IdentifierNotDeclaredException
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
    fun assigningIntToFloatThrowsException() {
        val (scope, start) = getScopeFromString("Float a = 5;")

        assertThrows<IllegalImplicitTypeConversionException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun intAdditionIntReturnsIntIsTypeCorrect(){
        val (scope, start) = getScopeFromString("Int a = 5 + 8;")

        TypeChecker(scope).start(start)
    }

    @Test
    fun boolEqualsBoolReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = true == false;")

        TypeChecker(scope).start(start)
    }

    @Test
    fun intEqualsIntReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = 6 == 8;")

        TypeChecker(scope).start(start)
    }

    @Test
    fun boolAdditionBoolThrowsException() {
        val (scope, start) = getScopeFromString("Bool a = true + true;")

        assertThrows<IncompatibleOperatorException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun intAdditionBoolThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 6 + true;")

        assertThrows<IncompatibleOperatorException> { TypeChecker(scope).start(start) }
    }

    @Test
    fun chainedGreaterThanOperationsThrowsException() {
        val (scope, start) = getScopeFromString("Bool a = 1 < 2 < 3;")

        assertThrows<IncompatibleOperatorException> { TypeChecker(scope).start(start)}
    }

    @Test
    fun chainedEqualsOperationsReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = 1 == 2 == (2 == 2);")

        TypeChecker(scope).start(start)
    }

    @Test
    fun intEqualsFloatThrowsException() {
        val (st, start) = getScopeFromString("Bool a = 6 == 6.6;")

        assertThrows<IncompatibleOperatorException> { TypeChecker(st).start(start) }
    }

    @Test
    fun functionWithParameterTypesBoolIntCalledWithArgumentTypesIntBoolThrowsException() {
        val (st, start) = getScopeFromString("fun foo(Bool a, Int b) {;} every (20ms) { foo(3, false); }")

        assertThrows<IdentifierNotDeclaredException> {TypeChecker(st).start(start)}
    }

    @Test
    fun everyStructuresExpressionTypeIntThrowsException() {
        val (st, start) = getScopeFromString("every (45 + 8) { ; }")

        assertThrows<IllegalImplicitTypeConversionException> {TypeChecker(st).start(start)}
    }

    @Test
    fun ifStatementGivenIntExpressionThrowsException() {
        val (st, start) = getScopeFromString("every(20ms) { if (45 + 8) ; }")

        assertThrows<IllegalImplicitTypeConversionException> {TypeChecker(st).start(start)}
    }

    @Test
    fun ifStatementGivenBoolExpressionIsTypeCorrect() {
        val (st, start) = getScopeFromString("every(20ms) { if (true); }")

        TypeChecker(st).start(start)
    }

    @Test
    fun intArray1DVarCanBeInitialisedWith1DIntArrayLiteral() {
        val (st, start) = getScopeFromString("Int[] a = [13, 14, 45, 6];")

        TypeChecker(st).start(start)
    }

    @Test
    fun intArrayInitialisedWith2DIntArrayLiteralThrowsException() {
        val (st, start) = getScopeFromString("Int[] a = [[13]];")

        assertThrows<IllegalImplicitTypeConversionException> {TypeChecker(st).start(start)}
    }

    @Test
    fun indexingIntArrayReturnsIntTypeIsTypeCorrect() {
        val (st, start) = getScopeFromString("Int a = [23, 14][0];")

        TypeChecker(st).start(start)
    }

    @Test
    fun intArrayAssignedToStringArrayThrowsException() {
        val (st, start) = getScopeFromString("String[] a = [23, 14];")

        assertThrows<IllegalImplicitTypeConversionException> {TypeChecker(st).start(start)}
    }

    fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder().buildSymbolTable(s), s)
    }

}