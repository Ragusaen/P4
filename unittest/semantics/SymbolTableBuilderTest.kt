package semantics

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import sablecc.lexer.Lexer
import sablecc.parser.Parser
import java.io.PushbackReader
import org.junit.jupiter.api.assertThrows
import kotlin.Exception

internal class SymbolTableBuilderTest {
    @Test
    fun symbolTableBuilderThrowsErrorWhenVariableHasAlreadyBeenDeclared() {
        val stb = SymbolTableBuilder()
        val input = "Int a = 8; Int a = 5;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierAlreadyDeclaredException> { stb.buildSymbolTable(s) }
    }

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclaration() {
        val stb = SymbolTableBuilder()
        val input = "Int b = a + 2;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }
}