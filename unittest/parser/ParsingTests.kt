package parser

import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.lexer.LexerException
import sablecc.node.Start
import sablecc.parser.Parser
import sablecc.parser.ParserException

internal class ParsingTests {

    @Test
    fun parseVariableDeclarationIsParsable() {
        val input = """
            Int a
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIntegerDeclarationIsParsable() {
        val input = """
            Int a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIntegerDeclarationNegativeLiteralIsParsable() {
        val input = """
            Int a = -5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseInt8DeclarationIsParsable() {
        val input = """
            Int8 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseInt16DeclarationIsParsable() {
        val input = """
            Int16 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseInt32DeclarationIsParsable() {
        val input = """
            Int32 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseInt64DeclarationIsParsable() {
        val input = """
            Int64 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseUintDeclarationIsParsable() {
        val input = """
            Uint a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseUint8DeclarationIsParsable() {
        val input = """
            Uint8 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseUint16DeclarationIsParsable() {
        val input = """
            Uint16 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseUint32DeclarationIsParsable() {
        val input = """
            Uint32 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseUint64DeclarationIsParsable() {
        val input = """
            Uint64 a = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFloatDeclarationIsParsable() {
        val input = """
            Float f = 5.5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFloatDeclarationNegativeNumberIsParsable() {
        val input = """
            Float f = -5.5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFloatDeclarationNoNumbersBeforeDotIsParsable() {
        val input = """
            Float f = .5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFloatDeclarationNoNumbersAfterDotIsParsable() {
        val input = """
            Float f = 5.
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFloatDeclarationOnlyDotThrowsException() {
        val input = """
            Float f = .
        """
        assertThrows<LexerException> { parseString(input) }
    }

    @Test
    fun parseTimeDeclarationMinutesIsParsable() {
        val input = """
            Time t = 5m
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationSecondsIsParsable() {
        val input = """
            Time t = 5s
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationHoursIsParsable() {
        val input = """
            Time t = 5h
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationMillisecondsIsParsable() {
        val input = """
            Time t = 5ms
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationDecimalIsParsable() {
        val input = """
            Time t = 5.6ms
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationNegativeTimeIsParsable() {
        val input = """
            Time t = -5.6ms
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTimeDeclarationInvalidTimeSuffixThrowsException() {
        val input = """
            Time t = 7t
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDIPDeclarationIsParsable() {
        val input = """
            DigitalInputPin p = D5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDOPDeclarationIsParsable() {
        val input = """
            DigitalOutputPin p = D5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAOPDeclarationIsParsable() {
        val input = """
            AnalogOutputPin p = A5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAIPDeclarationIsParsable() {
        val input = """
            AnalogInputPin p = A5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIdentifierAsDNumberThrowsException() {
        val input = """
           Time D5 = 5s
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseIdentifierAsANumberThrowsException() {
        val input = """
           Time A5 = 5s
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseMultipleDeclarationsPerLineIsParsable() {
        val input = """
           Int a, b = 5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseArrayDeclarationEmptyIsParsable() {
        val input = """
           Time[] t = []
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseArrayDeclarationWithValuesIsParsable() {
        val input = """
           Time[] t = [500ms, 400ms]
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseMultipleDimentionArrayDeclarationIsParsable() {
        val input = """
           Time[] t = [[200ms, 100ms], [400ms, 1s]]
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureNoDclsThrowsException() {
        val input = """
            template module id {
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureWithDclsThrowsException() {
        val input = """
            template module id {
                Int i = 5
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationWithModuleStructureNoDclsIsParsable() {
        val input = """
            template module id {
                every(5s)
                   set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTemplateModuleDeclarationWithModuleStructureWithDclsIsParsable() {
        val input = """
            template module id {
                Int i = 5
                every(5s) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTemplateModuleDeclarationModuleStructureWithDclsAfterThrowsException() {
        val input = """
            template module id {
                every(5s) set D5 to HIGH
                Int i = 5
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationOneParamIsParsable() {
        val input = """
            template module id(Int a) {
                every(5s) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseTemplateModuleDeclarationMultipleParamsIsParsable() {
        val input = """
            template module id(Int a, Int b) {
                every(5s) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseNamedModuleDeclarationIsParsable() {
        val input = """
            module id {
                every(5s) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAnonymousModuleDeclarationNoDclsIsParsable() {
        val input = """
            every(5s) set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseOnStructureIsParsable() {
        val input = """
            on(true) set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionDeclarationNoParamsNoReturnTypeIsParsable() {
        val input = """
           fun f() set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionDeclarationNoParamsWithReturnTypeIsParsable() {
        val input = """
           fun f(): Int set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionDeclarationOneParamIsParsable() {
        val input = """
           fun f(Int i) set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionDeclarationMultipleParamsIsParsable() {
        val input = """
           fun f(Int i, Float f) set D5 to HIGH
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIfStmtNoElseIsParsable() {
        val input = """
           fun f() {
                if(true) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIfStmtWithElseIsParsable() {
        val input = """
           fun f() {
                if(true) 
                    set D5 to HIGH 
                else set D6 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIfStmtWithBlockIsParsable() {
        val input = """
           fun f() {
                if(true){
                    set D5 to HIGH
                }
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseIfStmtWithNoConditionThrowsException() {
        val input = """
           fun f() {
                if() set D5 to HIGH
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseWhileStmtIsParsable() {
        val input = """
           fun f() {
                while(true) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseWhileStmtWithBlockIsParsable() {
        val input = """
           fun f() {
                while(true){
                    set D5 to HIGH
                }
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseWhileStmtWithNoConditionThrowsException() {
        val input = """
           fun f() {
                while() set D5 to HIGH
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseForStmtIsParsable() {
        val input = """
           fun f() {
                for(i in 1 to 4) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseForStmtWithStepIsParsable() {
        val input = """
           fun f() {
                for(i in 1 to 4 step 2) set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseForStmtWithBlockIsParsable() {
        val input = """
           fun f() {
                for(i in 1 to 4){
                    set D5 to HIGH
                }
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseForStmtWithNoConditionThrowsException() {
        val input = """
           fun f() {
                for() set D5 to HIGH
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDanglingElseIsParsable() {
        val input = """
           fun f() {
                if(true)
                    if(true) 
                        set D5 to HIGH
                    else 
                        set D6 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDanglingElseWithWhileIsParsable() {
        val input = """
           fun f() {
                if(true) 
                    while(true) 
                        if(true) 
                            set D5 to HIGH
                        else 
                            set D6 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDanglingElseWithForIsParsable() {
        val input = """
           fun f() {
                if(true) 
                    for(i in 1 to 4)
                        if(true) 
                            set D5 to HIGH
                        else 
                            set D6 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i = 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAssignToIdentifierIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i = i
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAdditionAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i += 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseSubtractionAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i -= 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDivisionAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i /= 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseMultiplicationAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i *= 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseModuloAssignStmtIsParsable() {
        val input = """
           fun f() {
                Int i = 5
                i %= 7
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseStmtInGlobalScopeThrowsException() {
        val input = """
            set D5 to HIGH
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseStartStmtIsParsable() {
        val input = """
           fun f() {
                DigitalOutputPin p = D5
                start p
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseStartStmtWithoutIdentifierThrowsException() {
        val input = """
           fun f() {
                start
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseStopStmtIsParsable() {
        val input = """
           fun f() {
                stop
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDelayStmtIsParsable() {
        val input = """
           fun f() {
                delay 500ms
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDelayStmtNoParamsThrowsExceptioni() {
        val input = """
           fun f() {
                delay
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDelayUntilStmtIsParsable() {
        val input = """
           fun f() {
                delay until true
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDelayUntilStmtNoParamsThrowsExceptioni() {
        val input = """
           fun f() {
                delay until
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseReturnStmtNoExprIsParsable() {
        val input = """
           fun f() {
                return
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseReturnStmtWithExprIsParsable() {
        val input = """
           fun f() {
                return 5
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionCallStmtNoParamsIsParsable() {
        val input = """
           fun f() {
                g()
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionCallStmtOneParamIsParsable() {
        val input = """
           fun f() {
                g(id)
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseFunctionCallStmtMultipleParamsIsParsable() {
        val input = """
           fun f() {
                g(id, id2)
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseBreakStmtIsParsable() {
        val input = """
           fun f() {
                break
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseContinueStmtIsParsable() {
        val input = """
           fun f() {
                continue
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseNoStmtIsParsable() {
        val input = """
           fun f() {
                
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseSetAnalogPinStmtIsParsable() {
        val input = """
           fun f() {
                set A2 to 1023
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseSetDigitalPinStmtIsParsable() {
        val input = """
           fun f() {
                set D5 to HIGH
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseOrExprIsParsable() {
        val input = """
           Bool b = true or false
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAndExprIsParsable() {
        val input = """
           Bool b = true and false
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseEqualsExprIsParsable() {
        val input = """
           Bool b = true == false
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseNotEqualsExprIsParsable() {
        val input = """
           Bool b = true != false
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseLessThanExprIsParsable() {
        val input = """
           Bool b = 5 < 8
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseGreaterThanExprIsParsable() {
        val input = """
           Bool b = 5 > 8
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseLessThanEqualsExprIsParsable() {
        val input = """
           Bool b = 5 <= 8
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseGreaterThanEqualsExprIsParsable() {
        val input = """
           Bool b = 5 >= 8
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseReadExprIsParsable() {
        val input = """
           Int i = read A2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseAdditionExprIsParsable() {
        val input = """
           Int i = 5 + 2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseSubtractionExprIsParsable() {
        val input = """
           Int i = 5 - 2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseMultiplicationExprIsParsable() {
        val input = """
           Int i = 5 * 2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseDivisionExprIsParsable() {
        val input = """
           Int i = 5 / 2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseModuloExprIsParsable() {
        val input = """
           Int i = 5 % 2
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parsePlusUnaryExprIsParsable() {
        val input = """
           Int i = 2 + +5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseMinusUnaryExprIsParsable() {
        val input = """
           Int i = 2 - --5
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseNegationExprIsParsable() {
        val input = """
           Bool b = !true
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseParenthesisExprIsParsable() {
        val input = """
           Int i = (5 + 2)
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseArrayIndexingExprIsParsable() {
        val input = """
           Int i = a[2]
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseSingleLineCommentIsParsable() {
        val input = """
            // Comment
           Int i = 4  
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseCommentInFunctionIsParsable() {
        val input = """
            fun a() {
                // Do
            }
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    @Test
    fun parseMultiLineCommentIsParsable() {
        val input = """
           /*
           Comment
           */
           Int i = 4
        """

        parseString(input)

        // Assertion occurs implicitly, test will pass if no exception is thrown
    }

    private fun parseString(input: String): Start {
        val newInput = (input + "\n").replace("(?m)^[ \t]*\r?\n".toRegex(), "")
        val lexer = StringLexer(newInput)
        val parser = Parser(lexer)
        return parser.parse()
    }
}
