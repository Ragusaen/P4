package semantics

import StringLexer
import org.junit.jupiter.api.Test
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.SymbolTable.SymbolTable
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.errors.IdentifierNotDeclaredError
import semantics.TypeChecking.errors.IllegalImplicitTypeConversionError
import semantics.TypeChecking.errors.IncompatibleOperatorError
import semantics.TypeChecking.TypeChecker
import semantics.TypeChecking.errors.ArrayInitializationError

internal class TypeCheckerTest {
    @Test
    fun assigningFloatToIntThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 5.5")

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(scope).run(start) }
    }

    @Test
    fun assigningIntToFloatThrowsException() {
        val (scope, start) = getScopeFromString("Float a = 5")

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(scope).run(start) }
    }

    @Test
    fun intAdditionIntReturnsIntIsTypeCorrect(){
        val (scope, start) = getScopeFromString("Int a = 5 + 8")

        TypeChecker(scope).run(start)
    }

    @Test
    fun boolEqualsBoolReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = true == false")

        TypeChecker(scope).run(start)
    }

    @Test
    fun intEqualsIntReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = 6 == 8")

        TypeChecker(scope).run(start)
    }

    @Test
    fun boolAdditionBoolThrowsException() {
        val (scope, start) = getScopeFromString("Bool a = true + true")

        assertThrows<IncompatibleOperatorError> { TypeChecker(scope).run(start) }
    }

    @Test
    fun intAdditionBoolThrowsException() {
        val (scope, start) = getScopeFromString("Int a = 6 + true")

        assertThrows<IncompatibleOperatorError> { TypeChecker(scope).run(start) }
    }

    @Test
    fun chainedGreaterThanOperationsThrowsException() {
        val (scope, start) = getScopeFromString("Bool a = 1 < 2 < 3")

        assertThrows<IncompatibleOperatorError> { TypeChecker(scope).run(start)}
    }

    @Test
    fun chainedEqualsOperationsReturnsBoolIsTypeCorrect() {
        val (scope, start) = getScopeFromString("Bool a = 1 == 2 == (2 == 2)")

        TypeChecker(scope).run(start)
    }

    @Test
    fun intEqualsFloatThrowsException() {
        val (st, start) = getScopeFromString("Bool a = 6 == 6.6")

        assertThrows<IncompatibleOperatorError> { TypeChecker(st).run(start) }
    }

    @Test
    fun functionWithParameterTypesBoolIntCalledWithArgumentTypesIntBoolThrowsException() {
        val (st, start) = getScopeFromString("fun foo(Bool a, Int b) {} every (20ms) { foo(3, false) }")

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(st).run(start)}
    }

    @Test
    fun everyStructuresExpressionTypeIntThrowsException() {
        val (st, start) = getScopeFromString("every (45 + 8) {  }")

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun ifStatementGivenIntExpressionThrowsException() {
        val (st, start) = getScopeFromString("every(20ms) { if (45 + 8)  }")

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun ifStatementGivenBoolExpressionIsTypeCorrect() {
        val (st, start) = getScopeFromString("every(20ms) { if (true) }")

        TypeChecker(st).run(start)
    }

    @Test
    fun intArray1DVarCanBeInitialisedWith1DIntArrayLiteral() {
        val (st, start) = getScopeFromString("Int[] a = [13, 14, 45, 6]")

        TypeChecker(st).run(start)
    }

    @Test
    fun intArrayInitialisedWith2DIntArrayLiteralThrowsException() {
        val (st, start) = getScopeFromString("Int[] a = [[13]]")

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun indexingIntArrayReturnsIntTypeIsTypeCorrect() {
        val (st, start) = getScopeFromString("Int a = [23, 14][0]")

        TypeChecker(st).run(start)
    }

    @Test
    fun intArrayAssignedToStringArrayThrowsException() {
        val (st, start) = getScopeFromString("String[] a = [23, 14]")

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun returnTypeNotMatchingFunctionTypeThrowsException() {
        val code =
        """
            fun a():Int {
                return "abc"
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun returnTypeMatchingFunctionTypeIsOk() {
        val code =
        """
            fun a():String {
                return "abc"
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun usingTemplateModuleNameThatIsNotDeclaredThrowsException() {
        val code =
        """
            module mod this
            
            template module that {
                every(100ms)
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(st).run(start)}
    }

    @Test
    fun usingTemplateModuleNameThatIsDeclaredIsOk() {
        val code =
        """
            module mod a
            
            template module a {
                every(100ms)
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun moduleInstancesWithMismatchingParametersThrowsException() {
        val code =
        """
            module mod a(50)
            
            template module a(Time t) {
                every(t)
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun moduleInstancesWithMatchingParametersIsOk() {
        val code =
        """
            module mod a("abc")
            
            template module a(String s) {
                every(50ms)
                    s += "a"
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun everyLoopWithExpressionOfNotTypeTimeThrowsException() {
        val code =
        """
            template module a {
                every(100.0)
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun everyLoopWithExpressionOfTypeTimeIsOk() {
        val code =
        """
            template module a {
                every(100ms)
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun ifStatementWithConditionOfNotTypeBoolThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    if("abc")
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun ifStatementWithConditionOfTypeBoolIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    if(true)
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun forStatementWithMiddleStatementNotOfTypeBoolThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    for(8)
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun forStatementWithMiddleStatementOfTypeBoolIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    for(true)
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun whileStatementWithConditionNotOfTypeBoolThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    while("yes")
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(st).run(start)}
    }

    @Test
    fun whileStatementWithConditionOfTypeBoolIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    while(true)
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun callingFunctionWithMismatchingParametersThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    a("abc")
            }
            
            fun a(Int a, Float f) {}
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(st).run(start)}
    }

    @Test
    fun callingFunctionWithMatchingParametersIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    a("abc")
            }
            
            fun a(String s) {}
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun declaringArrayWithoutSizeThrowsException() {
        val code =
        """
            Int[] a
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<ArrayInitializationError> {TypeChecker(st).run(start)}
    }

    @Test
    fun declaringArrayWithExplicitSizeIsOk() {
        val code =
        """
            Int[10] a
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun declaringArrayWithSizeFromInitializationIsOk() {
        val code =
        """
            Int[] a = [1, 2, 3]
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun initializingVariableWithMismatchingValueTypeThrowsException() {
        val code =
        """
            Int a = "abc"
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(st).run(start) }
    }

    @Test
    fun initializingVariableWithMatchingValueTypeIsOk() {
        val code =
        """
            Int a = 88
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun initializingAnalogInputPinWithValueNotOfTypeAnalogPinThrowsException() {
        val code =
        """
            AnalogInputPin aip = 40.5
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(st).run(start) }
    }

    @Test
    fun initializingAnalogInputPinWithValueOfTypeAnalogPinIsOk() {
        val code =
        """
            AnalogInputPin aip = A15
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
}

    @Test
    fun initializingDigitalInputPinWithValueNotOfTypeDigitalPinThrowsException() {
        val code =
        """
            DigitalInputPin aip = A8
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(st).run(start) }
    }

    @Test
    fun initializingDigitalInputPinWithValueOfTypeDigitalPinIsOk() {
        val code =
        """
            DigitalInputPin aip = D10
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfNotTypeBoolThrowsException() {
        val code =
                """
            Bool b = !(10 + 10)
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IncompatibleOperatorError> { TypeChecker(st).run(start) }
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfTypeBoolIsOk() {
        val code =
                """
            Bool b = !true
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfNotTypeIntOrFloatThrowsException() {
        val code =
        """
            Int b = +true
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IncompatibleOperatorError> { TypeChecker(st).run(start) }
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeIntIsOk() {
        val code =
        """
            Int a = +22
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeFloatIsOk() {
        val code =
        """
            Float a = +.9
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }

    @Test
    fun assignStatementExpressionNotMatchingVariableDeclaredTypeThrowsException() {
        val code =
        """
            fun a() {
                Int a
                a = "this"
            }
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(st).run(start) }
    }

    @Test
    fun assignStatementExpressionMatchingVariableDeclaredTypeIsOk() {
        val code =
        """
            fun a() {
                Int a
                a = 1024
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(st).run(start)
    }


    private fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        var newInput = input + "\n"
        newInput = input.replace("(?m)^[ \t]*\r?\n".toRegex(), "")
        val lexer = StringLexer(newInput)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder().buildSymbolTable(s), s)
    }
}