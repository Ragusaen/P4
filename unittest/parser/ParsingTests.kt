package parser

import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import sablecc.parser.Parser
import semantics.ContextualConstraints.ContextualConstraintAnalyzer
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationException
import java.lang.Exception

internal class ParsingTests {

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
        assertThrows<Exception> { parseString(input) }
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
        assertThrows<Exception> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureNoDclsThrowsException(){
        val input = """
            template module id {
            }
        """
        assertThrows<Exception> { parseString(input) }
    }

    @Test
    fun parseTemplateModuleDeclarationNoModuleStructureWithDclsThrowsException(){
        val input = """
            template module id {
                Int i = 5;
            }
        """
        assertThrows<Exception> { parseString(input) }
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

    fun parseString(input:String): Start {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        return parser.parse()
    }
}