package lexer

import org.junit.jupiter.api.Test
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

}