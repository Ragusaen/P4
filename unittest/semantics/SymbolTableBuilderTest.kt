package semantics

import StringLexer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.SymbolTable.Exceptions.IdentifierAlreadyDeclaredException
import semantics.SymbolTable.Exceptions.IdentifierUsedBeforeDeclarationException
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.Type

internal class SymbolTableBuilderTest {
    fun parseString(input: String): Start {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        return parser.parse()
    }

    @Test
    fun symbolTableBuilderThrowsErrorWhenVariableHasAlreadyBeenDeclared() {
        val stb = SymbolTableBuilder()
        val input = "Int a = 8; Int a = 5;"
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierAlreadyDeclaredException> { stb.buildSymbolTable(s) }
    }

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclaration() {
        val stb = SymbolTableBuilder()
        val input = "Int b = a + 2;"
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }

    @Test
    fun variableDeclaredAfterUse() {
        val stb = SymbolTableBuilder()
        val input = """
|           every (1000) {
                Int a = b - 3;
                Int b = 0;
            }
        """.trimMargin()
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }

    @Test
    fun useOfNonDeclaredFunctionThrowsDeclarationException() {
        val start = parseString("""
           every (2) {
                foo();
           }
        """.trimIndent())

        assertThrows<IdentifierUsedBeforeDeclarationException>{ SymbolTableBuilder().buildSymbolTable(start)}
    }

    @Test
    fun useOfDeclaredFunctionReturnsTableWithFunction() {
        val start = parseString("""
            fun foo(String s, Int i): String {
                return s;
            }
            
            every (2) {
                foo("ans", 42);
            }
        """.trimIndent())

        val st = SymbolTableBuilder().buildSymbolTable(start)

        st.openScope()
        assertNotNull(st.findVar("s"))
        assertNotNull(st.findVar("i"))
        st.closeScope()
        st.openScope()
        assertNotNull(st.findFun("foo", listOf(Type.String, Type.Int)))
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
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }


    @Test
    fun symbolTableBuilderThrowsErrorIfForLoopVariablesAreUsedInForLoopParens(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                every (1000) {
                    for (Int i = b; i < 2; i += 2) {
                        Int b = 1;
                    }
                }
            }
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()

        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }

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
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        val st = stb.buildSymbolTable(s)

        assertNotNull(st.findVar("a"))
        st.openScope()
        assertNotNull(st.findVar("a"))
    }

    @Test
    fun rootScopeVariablesUsedBeforeDeclarationIsOkay(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                Int b = a;
                every (1000) {
                    ; 
                }
            }
            Int a = 2;
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        val st = stb.buildSymbolTable(s)

        assertNotNull(st.findVar("a"))
        st.openScope()
        st.openScope()
        assertNotNull(st.findVar("b"))
    }

    @Test
    fun callingFunctionThatDoesntExistGivesException() {
        val stb = SymbolTableBuilder()
        val input = """
            every (1000ms) {
                foo();
            }
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        assertThrows<IdentifierUsedBeforeDeclarationException> { stb.buildSymbolTable(s) }
    }

    @Test
    fun functionCanBeRecursive() {
        val stb = SymbolTableBuilder()
        val input = """
            fun foo() {
                foo();
            }
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        val st = stb.buildSymbolTable(s)

        assertNotNull(st.findFun("foo", listOf()))
    }

    @Test
    fun instanceOfTemplateModuleCanBeDeclared(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                every (1000) {
                    ; 
                }
            }
            module thismodule thisinstance;
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        val st = stb.buildSymbolTable(s)

        assertNotNull(st.findTemplateModule("thismodule"))
        assertNotNull(st.findVar("thisinstance"))
    }

    @Test
    fun namedModuleCanBeDeclared() {
        val stb = SymbolTableBuilder()
        val input = """
            module thismodule {
                every (1000) {
                    ; 
                }
            }
        """
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val s = parser.parse()
        val st = stb.buildSymbolTable(s)

        assertNotNull(st.findVar("thismodule"))
    }
}