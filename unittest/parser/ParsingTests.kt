package parser

import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.lexer.LexerException
import sablecc.node.Start
import sablecc.parser.Parser
import sablecc.parser.ParserException
import semantics.ContextualConstraints.ContextualConstraintAnalyzer
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationException
import java.lang.Exception

internal class ParsingTests {

    @Test
    fun parseVariableDeclarationIsParseable(){
        val input = """
            Int a;
        """
        parseString(input)
    }

    @Test
    fun parseIntegerDeclarationIsParseable(){
        val input = """
            Int a = 5;
        """
        parseString(input)
    }

    @Test
    fun parseIntegerDeclarationNegativeLiteralIsParseable(){
        val input = """
            Int a = -5;
        """
        parseString(input)
    }

    @Test
    fun parseFloatDeclarationIsParseable(){
        val input = """
            Float f = 5.5;
        """
        parseString(input)
    }

    @Test
    fun parseFloatDeclarationNegativeNumberIsParseable(){
        val input = """
            Float f = -5.5;
        """
        parseString(input)
    }

    @Test
    fun parseFloatDeclarationNoNumbersBeforeDotIsParseable(){
        val input = """
            Float f = .5;
        """
        parseString(input)
    }

    @Test
    fun parseFloatDeclarationNoNumbersAfterDotIsParseable(){
        val input = """
            Float f = 5.;
        """
        parseString(input)
    }

    @Test
    fun parseFloatDeclarationOnlyDotThrowsException(){
        val input = """
            Float f = .;
        """
        assertThrows<LexerException> { parseString(input) }
    }

    @Test
    fun parseTimeDeclarationMinutesIsParseable(){
        val input = """
            Time t = 5m;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationSecondsIsParseable(){
        val input = """
            Time t = 5s;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationHoursIsParseable(){
        val input = """
            Time t = 5h;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationMillisecondsIsParseable(){
        val input = """
            Time t = 5ms;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationDecimalIsParseable(){
        val input = """
            Time t = 5.6ms;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationNegativeTimeIsParseable(){
        val input = """
            Time t = -5.6ms;
        """
        parseString(input)
    }

    @Test
    fun parseTimeDeclarationInvalidTimeSuffixThrowsException(){
        val input = """
            Time t = 7t;
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDIPDeclarationIsParseable(){
        val input = """
            DigitalInputPin p = D5;
        """
        parseString(input)
    }

    @Test
    fun parseDOPDeclarationIsParseable(){
        val input = """
            DigitalOutputPin p = D5;
        """
        parseString(input)
    }

    @Test
    fun parseAOPDeclarationIsParseable(){
        val input = """
            AnalogOutputPin p = A5;
        """
        parseString(input)
    }

    @Test
    fun parseAIPDeclarationIsParseable(){
        val input = """
            AnalogInputPin p = A5;
        """
        parseString(input)
    }

    @Test
    fun parseIdentifierAsDNumberThrowsException(){
        val input = """
           Time D5 = 5s; 
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseIdentifierAsANumberThrowsException(){
        val input = """
           Time A5 = 5s; 
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseMultipleDeclarationsPerLineIsParseable(){
        val input = """
           Int a, b = 5;
        """
        parseString(input)
    }

    @Test
    fun parseArrayDeclarationEmptyIsParseable(){
        val input = """
           Time[] t = [];
        """
        parseString(input)
    }

    @Test
    fun parseArrayDeclarationWithValuesIsParseable(){
        val input = """
           Time[] t = [500ms, 400ms];
        """
        parseString(input)
    }

    @Test
    fun parseMultipleDimentionArrayDeclarationIsParseable(){
        val input = """
           Time[] t = [[200ms, 100ms], [400ms, 1s]];
        """
        parseString(input)
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureNoDclsThrowsException(){
        val input = """
            template module id {
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureWithDclsThrowsException(){
        val input = """
            template module id {
                Int i = 5;
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationWithModuleStructureNoDclsIsParseable(){
        val input = """
            template module id {
                every(5s);
            }
        """
        parseString(input)
    }

    @Test
    fun parseTemplateModuleDeclarationWithModuleStructureWithDclsIsParseable(){
        val input = """
            template module id {
                Int i = 5;
                every(5s);
            }
        """
        parseString(input)
    }

    @Test
    fun parseTemplateModuleDeclarationModuleStructureWithDclsAfterThrowsException(){
        val input = """
            template module id {
                every(5s);
                Int i = 5;
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationOneParamIsParseable(){
        val input = """
            template module id(Int a) {
                every(5s);
            }
        """
        parseString(input)
    }

    @Test
    fun parseTemplateModuleDeclarationMultipleParamsIsParseable(){
        val input = """
            template module id(Int a, Int b) {
                every(5s);
            }
        """
        parseString(input)
    }

    @Test
    fun parseNamedModuleDeclarationIsParseable(){
        val input = """
            module id {
                every(5s);
            }
        """
        parseString(input)
    }

    @Test
    fun parseAnonymousModuleDeclarationNoDclsIsParseable(){
        val input = """
            every(5s);
        """
        parseString(input)
    }

    @Test
    fun parseFunctionDeclarationNoParamsNoReturnTypeIsParseable(){
        var input = """
           fun f() ;
        """
        parseString(input)
    }

    @Test
    fun parseFunctionDeclarationNoParamsWithReturnTypeIsParseable(){
        var input = """
           fun f(): Int ;
        """
        parseString(input)
    }

    @Test
    fun parseFunctionDeclarationOneParamIsParseable(){
        var input = """
           fun f(Int i) ;
        """
        parseString(input)
    }

    @Test
    fun parseFunctionDeclarationMultipleParamsIsParseable(){
        var input = """
           fun f(Int i, Float f) ;
        """
        parseString(input)
    }

    @Test
    fun parseIfStmtNoElseIsParseable(){
        var input = """
           fun f(){
                if(true) ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseIfStmtWithElseIsParseable(){
        var input = """
           fun f(){
                if(true) ; else ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseIfStmtWithBlockIsParseable(){
        var input = """
           fun f(){
                if(true){
                    ;
                }
            }
        """
        parseString(input)
    }

    @Test
    fun parseIfStmtWithNoConditionThrowsException(){
        var input = """
           fun f(){
                if() ;
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseWhileStmtIsParseable(){
        var input = """
           fun f(){
                while(true) ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseWhileStmtWithBlockIsParseable(){
        var input = """
           fun f(){
                while(true){
                    ;
                }
            }
        """
        parseString(input)
    }

    @Test
    fun parseWhileStmtWithNoConditionThrowsException(){
        var input = """
           fun f(){
                while() ;
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseForStmtIsParseable(){
        var input = """
           fun f(){
                for(;true;) ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseForStmtWithBlockIsParseable(){
        var input = """
           fun f(){
                for(;true;){
                    ;
                }
            }
        """
        parseString(input)
    }

    @Test
    fun parseForStmtWithNoConditionThrowsException(){
        var input = """
           fun f(){
                for() ;
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDanglingElseIsParseable(){
        var input = """
           fun f(){
                if(true) if(true); else;
            }
        """
        parseString(input)
    }

    @Test
    fun parseDanglingElseWithWhileIsParseable(){
        var input = """
           fun f(){
                if(true) while(true) if(true) ; else ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseDanglingElseWithForIsParseable(){
        var input = """
           fun f(){
                if(true) for(;true;) if(true) ; else ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i = 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseAssignToIdentifierIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i = i;
            }
        """
        parseString(input)
    }

    @Test
    fun parseAdditionAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i += 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseSubtractionAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i -= 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseDivisionAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i /= 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseMultiplicationAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i *= 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseModuloAssignStmtIsParseable(){
        var input = """
           fun f(){
                Int i = 5;
                i %= 7;
            }
        """
        parseString(input)
    }

    @Test
    fun parseStmtInGlobalScopeThrowsException(){
        var input = """
            ;
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseStartStmtIsParseable(){
        var input = """
           fun f(){
                DigitalOutputPin p = D5;
                start(p);
            }
        """
        parseString(input)
    }

    @Test
    fun parseStartStmtWithoutIdentifierThrowsException(){
        var input = """
           fun f(){
                start();
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseStopStmtIsParseable(){
        var input = """
           fun f(){
                stop;
            }
        """
        parseString(input)
    }

    @Test
    fun parseDelayStmtIsParseable(){
        var input = """
           fun f(){
                delay(500ms);
            }
        """
        parseString(input)
    }

    @Test
    fun parseDelayStmtNoParamsThrowsExceptioni(){
        var input = """
           fun f(){
                delay();
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseDelayUntilStmtIsParseable(){
        var input = """
           fun f(){
                delay until(500ms);
            }
        """
        parseString(input)
    }

    @Test
    fun parseDelayUntilStmtNoParamsThrowsExceptioni(){
        var input = """
           fun f(){
                delay until();
            }
        """
        assertThrows<ParserException> { parseString(input) }
    }

    @Test
    fun parseReturnStmtNoExprIsParseable(){
        var input = """
           fun f(){
                return;
            }
        """
        parseString(input)
    }

    @Test
    fun parseReturnStmtWithExprIsParseable(){
        var input = """
           fun f(){
                return 5;
            }
        """
        parseString(input)
    }

    @Test
    fun parseFunctionCallStmtNoParamsIsParseable(){
        var input = """
           fun f(){
                g();
            }
        """
        parseString(input)
    }

    @Test
    fun parseFunctionCallStmtOneParamIsParseable(){
        var input = """
           fun f(){
                g(id);
            }
        """
        parseString(input)
    }

    @Test
    fun parseFunctionCallStmtMultipleParamsIsParseable(){
        var input = """
           fun f(){
                g(id, id2);
            }
        """
        parseString(input)
    }

    @Test
    fun parseBreakStmtIsParseable(){
        var input = """
           fun f(){
                break;
            }
        """
        parseString(input)
    }

    @Test
    fun parseContinueStmtIsParseable(){
        var input = """
           fun f(){
                continue;
            }
        """
        parseString(input)
    }

    @Test
    fun parseNoStmtIsParseable(){
        var input = """
           fun f(){
                ;
            }
        """
        parseString(input)
    }

    @Test
    fun parseSetAnalogPinStmtIsParseable(){
        var input = """
           fun f(){
                set A2 to 1023;
            }
        """
        parseString(input)
    }

    @Test
    fun parseSetDigitalPinStmtIsParseable(){
        var input = """
           fun f(){
                set D5 to HIGH;
            }
        """
        parseString(input)
    }

    @Test
    fun parseOrExprIsParseable(){
        var input = """
           Bool b = true or false;
        """
        parseString(input)
    }

    @Test
    fun parseAndExprIsParseable(){
        var input = """
           Bool b = true and false;
        """
        parseString(input)
    }

    @Test
    fun parseEqualsExprIsParseable(){
        var input = """
           Bool b = true == false;
        """
        parseString(input)
    }

    @Test
    fun parseNotEqualsExprIsParseable(){
        var input = """
           Bool b = true != false;
        """
        parseString(input)
    }

    @Test
    fun parseLessThanExprIsParseable(){
        var input = """
           Bool b = 5 < 8;
        """
        parseString(input)
    }

    @Test
    fun parseGreaterThanExprIsParseable(){
        var input = """
           Bool b = 5 > 8;
        """
        parseString(input)
    }

    @Test
    fun parseLessThanEqualsExprIsParseable(){
        var input = """
           Bool b = 5 <= 8;
        """
        parseString(input)
    }

    @Test
    fun parseGreaterThanEqualsExprIsParseable(){
        var input = """
           Bool b = 5 >= 8;
        """
        parseString(input)
    }

    @Test
    fun parseReadExprIsParseable(){
        var input = """
           Int i = read A2;
        """
        parseString(input)
    }

    @Test
    fun parseAdditionExprIsParseable(){
        var input = """
           Int i = 5 + 2;
        """
        parseString(input)
    }

    @Test
    fun parseSubtractionExprIsParseable(){
        var input = """
           Int i = 5 - 2;
        """
        parseString(input)
    }

    @Test
    fun parseMultiplicationExprIsParseable(){
        var input = """
           Int i = 5 * 2;
        """
        parseString(input)
    }

    @Test
    fun parseDivisionExprIsParseable(){
        var input = """
           Int i = 5 / 2;
        """
        parseString(input)
    }

    @Test
    fun parseModuloExprIsParseable(){
        var input = """
           Int i = 5 % 2;
        """
        parseString(input)
    }

    @Test
    fun parsePlusUnaryExprIsParseable(){
        var input = """
           Int i = 2 + +5;
        """
        parseString(input)
    }

    @Test
    fun parseMinusUnaryExprIsParseable(){
        var input = """
           Int i = 2 + -5;
        """
        parseString(input)
    }

    @Test
    fun parseNegationExprIsParseable(){
        var input = """
           Bool b = !true;
        """
        parseString(input)
    }

    @Test
    fun parseParenthesisExprIsParseable(){
        var input = """
           Int i = (5 + 2);
        """
        parseString(input)
    }

    @Test
    fun parseArrayIndexingExprIsParseable(){
        var input = """
           Int i = a[2];
        """
        parseString(input)
    }

    fun parseString(input:String): Start {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        return parser.parse()
    }
}
