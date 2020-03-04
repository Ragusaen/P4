package lexer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals


internal class DumplingLexerTest {
    @Test
    fun single_token_Int_type_returns_INTTYPE() {
        val reader = ("Int").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.INTTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_String_type_returns_STRINGTYPE() {
        val reader = ("String").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.STRINGTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Float_type_returns_FLOATTYPE() {
        val reader = ("Float").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.FLOATTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Bool_type_returns_BOOLTYPE() {
        val reader = ("Bool").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.BOOLTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Char_type_returns_CHARTYPE() {
        val reader = ("Char").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.CHARTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_DigitalOutputPin_type_returns_DIGITALOUTPUTPINTYPE() {
        val reader = ("DigitalOutputPin").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.DIGITALOUTPUTPINTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_DigitalInputPin_type_returns_DIGITALINPUTPINTYPE() {
        val reader = ("DigitalInputPin").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.DIGITALINPUTPINTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_AnalogOutputPin_type_returns_ANALOGOUTPUTPINTYPE() {
        val reader = ("AnalogOutputPin").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ANALOGOUTPUTPINTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_AnalogInputPin_type_returns_ANALOGINPUTPINTYPE() {
        val reader = ("AnalogInputPin").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ANALOGINPUTPINTYPE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Time_type_returns_TIME() {
        val reader = ("Time").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.TIME,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Int_literal_returns_INTLITERAL() {
        val reader = ("123456789").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.INTLITERAL,0,0,"123456789")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_String_literal_returns_STRING() {
        val reader = ("\"This IS a StrInG\"").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.STRING,0,17,"This IS a StrInG")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Char_literal_returns_CHAR() {
        val reader = ("'g'").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.CHAR,0,2,"g")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Float_literal_returns_FLOATLITERAL() {
        val reader = ("12.78").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.FLOATLITERAL,0,0,"12.78")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_Bool_literal_returns_BOOLLITERAL() {
        val reader = ("true").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.BOOLLITERAL,0,0,"true")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_every_structure_returns_EVERY() {
        val reader = ("every").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.EVERY,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_on_structure_returns_ON() {
        val reader = ("on").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ON,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_if_structure_returns_IF() {
        val reader = ("if").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.IF,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_else_structure_returns_ELSE() {
        val reader = ("else").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ELSE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_while_structure_returns_WHILE() {
        val reader = ("while").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.WHILE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_for_structure_returns_FOR() {
        val reader = ("for").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.FOR,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_break_structure_returns_BREAK() {
        val reader = ("break").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.BREAK,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_continue_structure_returns_CONTINUE() {
        val reader = ("continue").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.CONTINUE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_template_structure_returns_TEMPLATE() {
        val reader = ("template").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.TEMPLATE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_fun_structure_returns_FUN() {
        val reader = ("fun").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.FUN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_module_structure_returns_MODULE() {
        val reader = ("module").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.MODULE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_rising_structure_returns_RISING() {
        val reader = ("rising").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.RISING,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_falling_structure_returns_FALLING() {
        val reader = ("falling").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.FALLING,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_delay_operator_returns_DELAY() {
        val reader = ("delay").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.DELAY,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_pin_operator_returns_PIN() {
        val reader = ("pin").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.PIN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_until_operator_returns_UNTIL() {
        val reader = ("until").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.UNTIL,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_set_operator_returns_SET() {
        val reader = ("set").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.SET,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_to_operator_returns_TO() {
        val reader = ("to").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.TO,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_read_operator_returns_READ() {
        val reader = ("read").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.READ,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_start_operator_returns_START() {
        val reader = ("start").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.START,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_stop_operator_returns_STOP() {
        val reader = ("stop").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.STOP,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_and_operator_returns_AND() {
        val reader = ("and").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.AND,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_or_operator_returns_OR() {
        val reader = ("or").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.OR,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_assign_operator_returns_ASSIGN() {
        val reader = ("=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_equal_operator_returns_EQUAL() {
        val reader = ("==").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.EQUAL,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_notequal_operator_returns_NOTEQUAL() {
        val reader = ("!=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.NOTEQUAL,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_addition_operator_returns_ADDITION() {
        val reader = ("+").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ADDITION,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_subtraction_operator_returns_SUBTRACTION() {
        val reader = ("-").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.SUBTRACTION,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_multiplication_operator_returns_MULTIPLICATION() {
        val reader = ("*").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.MULTIPLICATION,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_division_operator_returns_DIVISION() {
        val reader = ("/").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.DIVISION,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_modulo_operator_returns_MODULO() {
        val reader = ("%").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.MODULO,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_greaterthan_operator_returns_GREATERTHAN() {
        val reader = (">").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.GREATERTHAN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_lessthan_operator_returns_LESSTHAN() {
        val reader = ("<").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.LESSTHAN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_additionassign_operator_returns_ADDITIONASSIGN() {
        val reader = ("+=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.ADDITIONASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_subtractionassign_operator_returns_SUBTRACTIONASSIGN() {
        val reader = ("-=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.SUBTRACTIONASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_multiplicationassign_operator_returns_MULTIPLICATIONASSIGN() {
        val reader = ("*=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.MULTIPLICATIONASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_divisionassign_operator_returns_DIVISIONASSIGN() {
        val reader = ("/=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.DIVISIONASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_moduloassign_operator_returns_MODULOASSIGN() {
        val reader = ("%=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.MODULOASSIGN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_greaterthanorequalto_operator_returns_GREATERTHANOREQUALTO() {
        val reader = (">=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.GREATERTHANOREQUALTO,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_lessthanorequalto_operator_returns_LESSTHANOREQUALTO() {
        val reader = ("<=").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.LESSTHANOREQUALTO,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_semicolon_operator_returns_SEMICOLON() {
        val reader = (";").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.SEMICOLON,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_comma_operator_returns_COMMA() {
        val reader = (",").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.COMMA,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_not_operator_returns_NOT() {
        val reader = ("!").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.NOT, 0, 0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_lparen_operator_returns_LPAREN() {
        val reader = ("(").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.LPAREN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_rparen_operator_returns_RPAREN() {
        val reader = (")").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.RPAREN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_lbrace_operator_returns_LBRACE() {
        val reader = ("{").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.LBRACE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_rbrace_operator_returns_RBRACE() {
        val reader = ("}").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.RBRACE,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_lbracket_operator_returns_LBRACKET() {
        val reader = ("[").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.LBRACKET,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_rbracket_operator_returns_RBRACKET() {
        val reader = ("]").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.RBRACKET,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_return_operator_returns_RETURN() {
        val reader = ("return").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.RETURN,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_identifier_returns_IDENTIFIER() {
        val reader = ("IdenTIfier").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.IDENTIFIER,0,0,"IdenTIfier")
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_colon_operator_returns_COLON() {
        val reader = (":").reader()
        val lexer = DumplingLexer(reader)
        val expected = Symbol(SymType.COLON,0,0)
        assertEquals(expected, lexer.yylex())
    }
    @Test
    fun single_token_timeliteral_returns_TIMELITERAL() {
        val reader = ("12ms").reader()
        val expected = Symbol(SymType.TIMELITERAL,0,0,"12ms")
        val lexer = DumplingLexer(reader)

        val actual = lexer.yylex()

        assertEquals(expected, actual)
    }
    @Test
    fun anonymous_module_every_timeliteral_increment_global_variable() {
        val reader = "Int a = 0; every 1000ms { a += 1; }".reader()
        val expected = listOf(Symbol(SymType.INTTYPE, 0, 0),
                Symbol(SymType.IDENTIFIER, 0,4, "a"),
                Symbol(SymType.ASSIGN, 0, 6),
                Symbol(SymType.INTLITERAL, 0, 8, "0"),
                Symbol(SymType.SEMICOLON, 0, 9),
                Symbol(SymType.EVERY, 0, 11),
                Symbol(SymType.TIMELITERAL, 0, 17, "1000ms"),
                Symbol(SymType.LBRACE, 0, 24),
                Symbol(SymType.IDENTIFIER, 0, 26, "a"),
                Symbol(SymType.ADDITIONASSIGN, 0, 28),
                Symbol(SymType.INTLITERAL, 0, 31, "1"),
                Symbol(SymType.SEMICOLON, 0, 32),
                Symbol(SymType.RBRACE, 0, 34)
        )
        val lexer = DumplingLexer(reader)

        val actual = Array(expected.size) { i -> lexer.yylex()!!}.toList()

        assertEquals(expected, actual)
        assertEquals(null, lexer.yylex())
    }

    @Test
    fun anonymous_module_every_timeliteral_increment_global_variable_interrupted_by_whitespace() {
        val reader = "Int\n a = 0\t; every    \n\n\n1000ms \t{\t a\n +=\n 1\n; \t\t      }".reader()
        val expected = listOf(Symbol(SymType.INTTYPE, 0, 0),
                Symbol(SymType.IDENTIFIER, 0,4, "a"),
                Symbol(SymType.ASSIGN, 0, 6),
                Symbol(SymType.INTLITERAL, 0, 8, "0"),
                Symbol(SymType.SEMICOLON, 0, 9),
                Symbol(SymType.EVERY, 0, 11),
                Symbol(SymType.TIMELITERAL, 0, 17, "1000ms"),
                Symbol(SymType.LBRACE, 0, 24),
                Symbol(SymType.IDENTIFIER, 0, 26, "a"),
                Symbol(SymType.ADDITIONASSIGN, 0, 28),
                Symbol(SymType.INTLITERAL, 0, 31, "1"),
                Symbol(SymType.SEMICOLON, 0, 32),
                Symbol(SymType.RBRACE, 0, 34)
        )
        val lexer = DumplingLexer(reader)

        val actual = Array(expected.size) { i -> lexer.yylex()!!}.toList()

        assertEquals(expected, actual)
        assertEquals(null, lexer.yylex())
    }

    @Test
    fun exception() {
        val lexer = DumplingLexer(".!-".reader())

    }
}