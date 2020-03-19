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

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclarationAndDefinedInLowerScope(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                Int a = 3;
                every (1000) {
                    ; 
                }
            }
            Int b = a + 2;
        """
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }

    //TODO: UNCOMMENT WHEN BOOLEAN/CONDITIONS IS IMPLEMENTED
    /*Test
    fun symbolTableBuilderThrowsErrorIfForLoopVariablesAreUsedInForLoopParens(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                for(Int i = b; i < 2; i += 2){
                    Int b = 1;
                }
                every (1000) {
                    ; 
                }
            }
        """
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }*/

    @Test
    fun symbolTableContainsVariablesWithSameNameInDiffirentScopes(){
        val stb = SymbolTableBuilder()
        val input = """
            Int a = 2;
            template module thismodule {
                Int a = 3;
                every (1000) {
                    ; 
                }
            }
        """
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)

        val s = parser.parse()
        val scope = stb.buildSymbolTable(s)

        assertTrue(scope.containsKey("a"))
        assertTrue(scope.children[0].containsKey("a"))
    }
}