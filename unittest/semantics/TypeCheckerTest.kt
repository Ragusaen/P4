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
        val input = "Int a = 5.5"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), scope).run(start) }
    }

    @Test
    fun assigningIntToFloatThrowsException() {
        val input = "Float a = 5"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), scope).run(start) }
    }

    @Test
    fun intAdditionIntReturnsIntIsTypeCorrect(){
        val input = "Int a = 5 + 8"
        val (scope, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), scope).run(start)
    }

    @Test
    fun boolEqualsBoolReturnsBoolIsTypeCorrect() {
        val input = "Bool a = true == false"
        val (scope, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), scope).run(start)
    }

    @Test
    fun intEqualsIntReturnsBoolIsTypeCorrect() {
        val input = "Bool a = 6 == 8"
        val (scope, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), scope).run(start)
    }

    @Test
    fun boolAdditionBoolThrowsException() {
        val input ="Bool a = true + true"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), scope).run(start) }
    }

    @Test
    fun intAdditionBoolThrowsException() {
        val input = "Int a = 6 + true"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), scope).run(start) }
    }

    @Test
    fun chainedGreaterThanOperationsThrowsException() {
        val input = "Bool a = 1 < 2 < 3"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), scope).run(start)}
    }

    @Test
    fun chainedEqualsOperationsReturnsBoolIsTypeCorrect() {
        val input = "Bool a = 1 == 2 == (2 == 2)"
        val (scope, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), scope).run(start)
    }

    @Test
    fun intEqualsFloatThrowsException() {
        val input = "Bool a = 6 == 6.6"
        val (scope, start) = getScopeFromString(input)

        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), scope).run(start) }
    }

    @Test
    fun functionWithParameterTypesBoolIntCalledWithArgumentTypesIntBoolThrowsException() {
        val input = "fun foo(Bool a, Int b) {} every (20ms) { foo(3, false)\n }"
        val (st, start) = getScopeFromString(input)

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun everyStructuresExpressionTypeIntThrowsException() {
        val input = "every (45 + 8) { stop\n }"
        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun ifStatementGivenIntExpressionThrowsException() {
        val input = "every(20ms) { if (45 + 8) stop \n}"
        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun ifStatementGivenBoolExpressionIsTypeCorrect() {
        val input = "every(20ms) { if (true) stop \n}"
        val (st, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun intArray1DVarCanBeInitialisedWith1DIntArrayLiteral() {
        val input = "Int[] a = [13, 14, 45, 6]"
        val (st, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun intArrayInitialisedWith2DIntArrayLiteralThrowsException() {
        val input = "Int[] a = [[13]]"
        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun indexingIntArrayReturnsIntTypeIsTypeCorrect() {
        val input = "Int a = [23, 14][0]"
        val (st, start) = getScopeFromString(input)

        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun intArrayAssignedToStringArrayThrowsException() {
        val input = "String[] a = [23, 14]"
        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun returnTypeNotMatchingFunctionTypeThrowsException() {
        val input =
        """
            fun a():Int {
                return "abc"
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun returnTypeMatchingFunctionTypeIsOk() {
        val input =
        """
            fun a():String {
                return "abc"
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun usingTemplateModuleNameThatIsDeclaredIsOk() {
        val input =
        """
            module a mod
            
            template module a {
                every(100ms)
                    stop
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun moduleInstancesWithMismatchingParametersThrowsException() {
        val input =
        """
            module a mod(50)
            
            template module a(Time t) {
                every(t)
                    stop
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun moduleInstancesWithMatchingParametersIsOk() {
        val input =
        """
            module a mod("abc")
            
            template module a(String s) {
                every(50ms)
                    s += "a"
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun everyLoopWithExpressionOfNotTypeTimeThrowsException() {
        val input =
        """
            template module a {
                every(100.0)
                    stop
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun everyLoopWithExpressionOfTypeTimeIsOk() {
        val input =
        """
            template module a {
                every(100ms)
                    stop
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun ifStatementWithConditionOfNotTypeBoolThrowsException() {
        val input =
        """
            template module a {
                every(100ms)
                    if("abc")
                        stop
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun ifStatementWithConditionOfTypeBoolIsOk() {
        val input =
        """
            template module a {
                every(100ms)
                    if(true)
                        stop
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun onCannotTakePinAsExpression() {
        val input = """
            on (D3) {
                stop
            }
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun functionCanReturnNothing() {
        val input = """
            fun foo()
                return
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun forStatementWithStartValueNotOfTypeIntThrowsException() {
        val input =
        """
            template module a {
                every(100ms)
                    for(i in true to 8)
                        continue
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun forStatementWithEndValueNotOfTypeIntThrowsException() {
        val input =
                """
            template module a {
                every(100ms)
                    for(i in 1 to D5)
                        continue
            }
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun forStatementWithStepValueNotOfTypeIntThrowsException() {
        val input =
                """
            template module a {
                every(100ms)
                    for(i in 1 to 8 step true)
                        continue
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun forStatementWithValuesOfTypeIntIsOk() {
        val input =
        """
            template module a {
                every(100ms)
                    for(i in 1 to 8 step 2)
                        continue
            }
        """
        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun whileStatementWithConditionNotOfTypeBoolThrowsException() {
        val input =
        """
            template module a {
                every(100ms)
                    while("yes")
                        continue
            }
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun whileStatementWithConditionOfTypeBoolIsOk() {
        val input =
        """
            template module a {
                every(100ms)
                    while(true)
                        stop
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun callingFunctionWithMismatchingParametersThrowsException() {
        val input =
        """
            template module a {
                every(100ms)
                    a("abc")
            }
            
            fun a(Int a, Float f) {}
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IdentifierNotDeclaredError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun callingFunctionWithMatchingParametersIsOk() {
        val input =
        """
            template module a {
                every(100ms)
                    a("abc")
            }
            
            fun a(String s) {}
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun declaringArrayWithoutSizeThrowsException() {
        val input =
        """
            Int[] a
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<ArrayInitializationError> {TypeChecker(ErrorHandler(input), st).run(start)}
    }

    @Test
    fun declaringArrayWithExplicitSizeIsOk() {
        val input =
        """
            Int[10] a
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun declaringArrayWithSizeFromInitializationIsOk() {
        val input =
        """
            Int[] a = [1, 2, 3]
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun initializingVariableWithMismatchingValueTypeThrowsException() {
        val input =
        """
            Int a = "abc"
        """

        val (st, start) = getScopeFromString(input)

        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun initializingVariableWithMatchingValueTypeIsOk() {
        val input =
        """
            Int a = 88
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun initializingAnalogInputPinWithValueNotOfTypeAnalogPinThrowsException() {
        val input =
        """
            AnalogInputPin aip = 40.5
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun initializingAnalogInputPinWithValueOfTypeAnalogPinIsOk() {
        val input =
        """
            AnalogInputPin aip = A15
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
}

    @Test
    fun initializingDigitalInputPinWithValueNotOfTypeDigitalPinThrowsException() {
        val input =
        """
            DigitalInputPin aip = A8
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun initializingDigitalInputPinWithValueOfTypeDigitalPinIsOk() {
        val input =
        """
            DigitalInputPin aip = D10
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfNotTypeBoolThrowsException() {
        val input =
                """
            Bool b = !(10 + 10)
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun applyingUnaryNotOperatorOnExpressionOfTypeBoolIsOk() {
        val input =
                """
            Bool b = !true
        """
        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfNotTypeIntOrFloatThrowsException() {
        val input =
        """
            Int b = +true
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IncompatibleOperatorError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeIntIsOk() {
        val input =
        """
            Int a = +22
        """
        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun applyingUnaryPlusOperatorOnExpressionOfTypeFloatIsOk() {
        val input =
        """
            Float a = +.9
        """
        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun assignStatementExpressionNotMatchingVariableDeclaredTypeThrowsException() {
        val input =
        """
            fun a() {
                Int a
                a = "this"
            }
        """

        val (st, start) = getScopeFromString(input)
        assertThrows<IllegalImplicitTypeConversionError> { TypeChecker(ErrorHandler(input), st).run(start) }
    }

    @Test
    fun assignStatementExpressionMatchingVariableDeclaredTypeIsOk() {
        val input =
        """
            fun a() {
                Int a
                a = 1024
            }
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    @Test
    fun assigningIntToAnotherIntTypeIsOk(){
        val input =
                """
            Int64 i = 254567
            Int8 k = i
        """

        val (st, start) = getScopeFromString(input)
        TypeChecker(ErrorHandler(input), st).run(start)
    }

    private fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder(ErrorHandler(input)).buildSymbolTable(s), s)
    }
}