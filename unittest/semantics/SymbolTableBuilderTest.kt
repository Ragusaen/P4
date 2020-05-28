package semantics

import ErrorHandler
import StringLexer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import sablecc.parser.Parser
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import symboltable.SymbolTable
import symboltable.errors.IdentifierAlreadyDeclaredError
import symboltable.errors.IdentifierUsedBeforeDeclarationError
import symboltable.SymbolTableBuilder
import typeChecking.Type
import typeChecking.errors.IdentifierNotDeclaredError

internal class SymbolTableBuilderTest {
    @Test
    fun symbolTableBuilderThrowsErrorWhenVariableHasAlreadyBeenDeclared() {
        val input = "Int a = 8\n Int a = 5"

        assertThrows<IdentifierAlreadyDeclaredError> { getSymbolTableFromString(input) }
    }

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclaration() {
        val input = "Int b = a + 2"

        assertThrows<IdentifierUsedBeforeDeclarationError> { getSymbolTableFromString(input) }
    }

    @Test
    fun variableDeclaredAfterUse() {
        val input = """
            every (1000) {
                Int a = b - 3
                Int b = 0
            }
        """.trimMargin()
        assertThrows<IdentifierUsedBeforeDeclarationError> { getSymbolTableFromString(input) }
    }

    @Test
    fun useOfNonDeclaredFunctionThrowsDeclarationException() {
        val input = """
           every (2) {
                foo()
           }
        """

        assertThrows<IdentifierUsedBeforeDeclarationError>{ getSymbolTableFromString(input)}
    }

    @Test
    fun useOfDeclaredFunctionReturnsTableWithFunction() {
        val input = """
            fun foo(String s, Int i): String {
                return s
            }
            
            every (2) {
                foo("ans", 42)
            }
        """

        val st = getSymbolTableFromString(input).first

        st.openScope()
        assertNotNull(st.findVar("s"))
        assertNotNull(st.findVar("i"))
        st.closeScope()
        st.openScope()
        assertNotNull(st.findFun("foo", listOf(Type.String, Type.Int)))
    }

    @Test
    fun symbolTableBuilderThrowsErrorIfVariableIsUsedBeforeDeclarationAndDefinedInLowerScope(){
        val input = """
            template module thismodule {
                Int a = 3
                every (1000) {
                     
                }
            }
            Int b = a + 2
        """

        assertThrows<IdentifierUsedBeforeDeclarationError> {  getSymbolTableFromString(input) }
    }

    @Test
    fun symbolTableContainsVariablesWithSameNameInDifferentScopes(){
        val input = """
            Int a = 2
            template module thismodule {
                Int a = 3
                every (1000) {
                     
                }
            }
        """
        
        val st = getSymbolTableFromString(input).first

        assertNotNull(st.findVar("a"))
        st.openScope()
        assertNotNull(st.findVar("a"))
    }

    @Test
    fun rootScopeVariablesUsedBeforeDeclarationIsOkay(){
        val input = """
            template module thismodule {
                Int b = a
                every (1000) {
                     
                }
            }
            Int a = 2
        """

        val st = getSymbolTableFromString(input).first

        assertNotNull(st.findVar("a"))
        st.openScope()
        st.openScope()
        assertNotNull(st.findVar("b"))
    }
    @Test
    fun functionCanBeUsedBeforeBeingDeclared() {
        val input = """
            Time a = foo()
            
            fun foo() {
                return 100ms
            }
        """
        getSymbolTableFromString(input)
    }

    @Test
    fun callingFunctionThatDoesntExistGivesException() {
        val input = """
            every (1000ms) {
                foo()
            }
        """
        assertThrows<IdentifierUsedBeforeDeclarationError> {  getSymbolTableFromString(input) }
    }

    @Test
    fun functionCanBeRecursive() {
        val input = """
            fun foo() {
                foo()
            }
        """

        val st = getSymbolTableFromString(input).first

        assertNotNull(st.findFun("foo", listOf()))
    }

    @Test
    fun templateModuleCanBeDeclared(){
        val input = """
            template module thismodule {
                Int a = 0
                every (1000)
                    a += 1
            }
        """

        val st = getSymbolTableFromString(input).first

        assertNotNull(st.findTemplateModule("thismodule"))
    }

    @Test
    fun usingTemplateModuleNameThatIsNotDeclaredThrowsException() {
        val input =
                """
            module mod this
            
            template module that {
                every(100ms)
                    stop
            }
        """

        assertThrows<IdentifierNotDeclaredError> { val (st, start) = getSymbolTableFromString(input) }
    }

    @Test
    fun functionMustBeDeclaredToUse() {
        val input = """
            every (1s) {
                foo()
            }
        """

        assertThrows<IdentifierUsedBeforeDeclarationError> {getSymbolTableFromString(input).first}
    }

    @Test
    fun functionCanBeUsedAboveDecleration() {
        val input = """
            every (1s) {
                foo()
            }
            
            fun foo()
                return
        """

        getSymbolTableFromString(input).first
    }

    @Test
    fun functionCanBeCalledFromInitWhenDeclaredAfter() {
        val input = """
            init {
                foo()
            }
            
            fun foo()
                return
        """

        getSymbolTableFromString(input).first
    }

    @Test
    fun functionCanBeCalledFromInVariableDeclerationWhenDeclaredAfter() {
        val input = """
            Int a = foo()
            
            fun foo(): Int
                return 3
        """

        getSymbolTableFromString(input).first
    }


    private fun getSymbolTableFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        return Pair(SymbolTableBuilder(ErrorHandler(input)).buildSymbolTable(s), s)
    }
}