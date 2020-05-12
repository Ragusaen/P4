package semantics

import StringLexer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import semantics.SymbolTable.SymbolTable
import semantics.SymbolTable.errors.IdentifierAlreadyDeclaredError
import semantics.SymbolTable.errors.IdentifierUsedBeforeDeclarationError
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.Type

internal class SymbolTableBuilderTest {
    fun parseString(input: String): Start {
        var newInput = input + "\n"
        newInput = input.replace("(?m)^[ \t]*\r?\n".toRegex(), "")
        val lexer = StringLexer(newInput)
        val parser = Parser(lexer)
        return parser.parse()
    }

    @Test
    fun symbolTableBuilderThrowsErrorWhenVariableHasAlreadyBeenDeclared() {
        val stb = SymbolTableBuilder()
        val input = "Int a = 8\n Int a = 5"

        assertThrows<IdentifierAlreadyDeclaredError> { getScopeFromString(input) }
    }

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclaration() {
        val stb = SymbolTableBuilder()
        val input = "Int b = a + 2"

        assertThrows<IdentifierUsedBeforeDeclarationError> { getScopeFromString(input) }
    }

    @Test
    fun variableDeclaredAfterUse() {
        val input = """
            every (1000) {
                Int a = b - 3
                Int b = 0
            }
        """.trimMargin()
        assertThrows<IdentifierUsedBeforeDeclarationError> { getScopeFromString(input) }
    }

    @Test
    fun useOfNonDeclaredFunctionThrowsDeclarationException() {
        val start = parseString("""
           every (2) {
                foo()
           }
        """.trimIndent())

        assertThrows<IdentifierUsedBeforeDeclarationError>{ SymbolTableBuilder().buildSymbolTable(start)}
    }

    @Test
    fun useOfDeclaredFunctionReturnsTableWithFunction() {
        val start = parseString("""
            fun foo(String s, Int i): String {
                return s
            }
            
            every (2) {
                foo("ans", 42)
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
                Int a = 3
                every (1000) {
                     
                }
            }
            Int b = a + 2
        """
        assertThrows<IdentifierUsedBeforeDeclarationError> {  getScopeFromString(input) }
    }


    @Test
    fun symbolTableBuilderThrowsErrorIfForLoopVariablesAreUsedInForLoopParens(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                every (1000) {
                    for (Int i = b i < 2 i += 2) {
                        Int b = 1
                    }
                }
            }
        """
        assertThrows<IdentifierUsedBeforeDeclarationError> {  getScopeFromString(input) }
    }

    @Test
    fun symbolTableContainsVariablesWithSameNameInDiffirentScopes(){
        val stb = SymbolTableBuilder()
        val input = """
            Int a = 2
            template module thismodule {
                Int a = 3
                every (1000) {
                     
                }
            }
        """
        
        val st = getScopeFromString(input).first

        assertNotNull(st.findVar("a"))
        st.openScope()
        assertNotNull(st.findVar("a"))
    }

    @Test
    fun rootScopeVariablesUsedBeforeDeclarationIsOkay(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                Int b = a
                every (1000) {
                     
                }
            }
            Int a = 2
        """
        val st = getScopeFromString(input).first

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
                foo()
            }
        """
        assertThrows<IdentifierUsedBeforeDeclarationError> {  getScopeFromString(input) }
    }

    @Test
    fun functionCanBeRecursive() {
        val stb = SymbolTableBuilder()
        val input = """
            fun foo() {
                foo()
            }
        """
        val st = getScopeFromString(input).first

        assertNotNull(st.findFun("foo", listOf()))
    }

    @Test
    fun instanceOfTemplateModuleCanBeDeclared(){
        val stb = SymbolTableBuilder()
        val input = """
            template module thismodule {
                every (1000) {
                     
                }
            }
            module thismodule thisinstance
        """
        val st = getScopeFromString(input).first

        assertNotNull(st.findTemplateModule("thismodule"))
        assertNotNull(st.findVar("thisinstance"))
    }

    @Test
    fun namedModuleCanBeDeclared() {
        val stb = SymbolTableBuilder()
        val input = """
            module thismodule {
                every (1000) {
                     
                }
            }
        """
        val st = getScopeFromString(input).first

        assertNotNull(st.findVar("thismodule"))
    }

    private fun getScopeFromString(input:String):Pair<SymbolTable, Start> {
        val newInput = (input + "\n").replace("(?m)^[ \t]*\r?\n".toRegex(), "")
        val lexer = StringLexer(newInput)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder().buildSymbolTable(s), s)
    }
}