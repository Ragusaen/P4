package semantics

import ErrorHandler
import StringLexer
import org.junit.jupiter.api.Test
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.symbolTable.SymbolTable
import semantics.symbolTable.SymbolTableBuilder
import semantics.typeChecking.errors.IdentifierNotDeclaredError
import semantics.typeChecking.errors.IllegalImplicitTypeConversionError
import semantics.typeChecking.errors.IncompatibleOperatorError
import semantics.typeChecking.TypeChecker
import semantics.typeChecking.errors.ArrayInitializationError

internal class TypeCheckerTest {
    @Test
    fun assigningFloatToIntThrowsException() {
        val code = "Int a = 5.5"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), scope).run(start) }
    }

    @Test
    fun assigningIntToFloatThrowsException() {
        val code = "Float a = 5"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), scope).run(start) }
    }

    @Test
    fun intAdditionIntReturnsIntIsTypeCorrect(){
        val code = "Int a = 5 + 8"
        val (scope, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), scope).run(start)
    }

    @Test
    fun boolEqualsBoolReturnsBoolIsTypeCorrect() {
        val code = "Bool a = true == false"
        val (scope, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), scope).run(start)
    }

    @Test
    fun intEqualsIntReturnsBoolIsTypeCorrect() {
        val code = "Bool a = 6 == 8"
        val (scope, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), scope).run(start)
    }

    @Test
    fun boolAdditionBoolThrowsException() {
        val code ="Bool a = true + true"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), scope).run(start) }
    }

    @Test
    fun intAdditionBoolThrowsException() {
        val code = "Int a = 6 + true"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), scope).run(start) }
    }

    @Test
    fun chainedGreaterThanOperationsThrowsException() {
        val code = "Bool a = 1 < 2 < 3"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), scope).run(start)}
    }

    @Test
    fun chainedEqualsOperationsReturnsBoolIsTypeCorrect() {
        val code = "Bool a = 1 == 2 == (2 == 2)"
        val (scope, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), scope).run(start)
    }

    @Test
    fun intEqualsFloatThrowsException() {
        val code = "Bool a = 6 == 6.6"
        val (scope, start) = getScopeFromString(code)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), scope).run(start) }
    }

    @Test
    fun functionWithParameterTypesBoolIntCalledWithArgumentTypesIntBoolThrowsException() {
        val code = "fun foo(Bool a, Int b) {} every (20ms) { foo(3, false)\n }"
        val (st, start) = getScopeFromString(code)

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun everyStructuresExpressionTypeIntThrowsException() {
        val code = "every (45 + 8) { stop\n }"
        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun ifStatementGivenIntExpressionThrowsException() {
        val code = "every(20ms) { if (45 + 8) stop \n}"
        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun ifStatementGivenBoolExpressionIsTypeCorrect() {
        val code = "every(20ms) { if (true) stop \n}"
        val (st, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun intArray1DVarCanBeInitialisedWith1DIntArrayLiteral() {
        val code = "Int[] a = [13, 14, 45, 6]"
        val (st, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun intArrayInitialisedWith2DIntArrayLiteralThrowsException() {
        val code = "Int[] a = [[13]]"
        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun indexingIntArrayReturnsIntTypeIsTypeCorrect() {
        val code = "Int a = [23, 14][0]"
        val (st, start) = getScopeFromString(code)

        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun intArrayAssignedToStringArrayThrowsException() {
        val code = "String[] a = [23, 14]"
        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
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

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
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
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun usingTemplateModuleNameThatIsDeclaredIsOk() {
        val code =
        """
            module a mod
            
            template module a {
                every(100ms)
                    stop
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun moduleInstancesWithMismatchingParametersThrowsException() {
        val code =
        """
            module a mod(50)
            
            template module a(Time t) {
                every(t)
                    stop
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun moduleInstancesWithMatchingParametersIsOk() {
        val code =
        """
            module a mod("abc")
            
            template module a(String s) {
                every(50ms)
                    s += "a"
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun everyLoopWithExpressionOfNotTypeTimeThrowsException() {
        val code =
        """
            template module a {
                every(100.0)
                    stop
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun everyLoopWithExpressionOfTypeTimeIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    stop
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun ifStatementWithConditionOfNotTypeBoolThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    if("abc")
                        stop
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun ifStatementWithConditionOfTypeBoolIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    if(true)
                        stop
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun forStatementWithStartValueNotOfTypeIntThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    for(i in true to 8)
                        continue
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun forStatementWithEndValueNotOfTypeIntThrowsException() {
        val code =
                """
            template module a {
                every(100ms)
                    for(i in 1 to D5)
                        continue
            }
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun forStatementWithStepValueNotOfTypeIntThrowsException() {
        val code =
                """
            template module a {
                every(100ms)
                    for(i in 1 to 8 step true)
                        continue
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun forStatementWithValuesOfTypeIntIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    for(i in 1 to 8 step 2)
                        continue
            }
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun whileStatementWithConditionNotOfTypeBoolThrowsException() {
        val code =
        """
            template module a {
                every(100ms)
                    while("yes")
                        continue
            }
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun whileStatementWithConditionOfTypeBoolIsOk() {
        val code =
        """
            template module a {
                every(100ms)
                    while(true)
                        stop
            }
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
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

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(ErrorHandler(code), st).run(start)}
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
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun declaringArrayWithoutSizeThrowsException() {
        val code =
        """
            Int[] a
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<ArrayInitializationError> {TypeChecker(ErrorHandler(code), st).run(start)}
    }

    @Test
    fun declaringArrayWithExplicitSizeIsOk() {
        val code =
        """
            Int[10] a
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun declaringArrayWithSizeFromInitializationIsOk() {
        val code =
        """
            Int[] a = [1, 2, 3]
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun initializingVariableWithMismatchingValueTypeThrowsException() {
        val code =
        """
            Int a = "abc"
        """

        val (st, start) = getScopeFromString(code)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), st).run(start) }
    }

    @Test
    fun initializingVariableWithMatchingValueTypeIsOk() {
        val code =
        """
            Int a = 88
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun initializingAnalogInputPinWithValueNotOfTypeAnalogPinThrowsException() {
        val code =
        """
            AnalogInputPin aip = 40.5
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), st).run(start) }
    }

    @Test
    fun initializingAnalogInputPinWithValueOfTypeAnalogPinIsOk() {
        val code =
        """
            AnalogInputPin aip = A15
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
}

    @Test
    fun initializingDigitalInputPinWithValueNotOfTypeDigitalPinThrowsException() {
        val code =
        """
            DigitalInputPin aip = A8
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), st).run(start) }
    }

    @Test
    fun initializingDigitalInputPinWithValueOfTypeDigitalPinIsOk() {
        val code =
        """
            DigitalInputPin aip = D10
        """

        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfNotTypeBoolThrowsException() {
        val code =
                """
            Bool b = !(10 + 10)
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), st).run(start) }
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfTypeBoolIsOk() {
        val code =
                """
            Bool b = !true
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfNotTypeIntOrFloatThrowsException() {
        val code =
        """
            Int b = +true
        """

        val (st, start) = getScopeFromString(code)
        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(code), st).run(start) }
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeIntIsOk() {
        val code =
        """
            Int a = +22
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeFloatIsOk() {
        val code =
        """
            Float a = +.9
        """
        val (st, start) = getScopeFromString(code)
        TypeChecker(ErrorHandler(code), st).run(start)
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
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(code), st).run(start) }
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
        TypeChecker(ErrorHandler(code), st).run(start)
    }


    private fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder(ErrorHandler(input)).buildSymbolTable(s), s)
    }
}