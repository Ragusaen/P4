package semantics

import ErrorHandler
import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import sablecc.parser.Parser
import semantics.contextualConstraints.ContextualConstraintAnalyzer
import semantics.contextualConstraints.errors.LoopJumpOutOfLoopError
import semantics.contextualConstraints.errors.ModuleStatementUsedInFunctionException
import semantics.contextualConstraints.errors.MultipleInitsError
import semantics.contextualConstraints.errors.ReturnOutOfFunctionDeclarationError
import semantics.symbolTable.SymbolTable
import semantics.symbolTable.SymbolTableBuilder

internal class ContextualConstraintsTest {
    @Test
    fun returnOutOfFunctionDeclarationThrowsException() {
        val code =
        """
            every (100ms) {
                return
            }
        """

        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(code)
        assertThrows<ReturnOutOfFunctionDeclarationError> { ContextualConstraintAnalyzer(ErrorHandler(code), st).caseStart(start) }
    }

    @Test
    fun returnInsideFunctionDeclarationIsOkay() {
        val code =
        """
            fun foo():Int {
                return 5
            }
        """

        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(code)
        ContextualConstraintAnalyzer(ErrorHandler(code), st).caseStart(start)
    }

    @Test
    fun breakAndContinueInsideNestedLoopsIsOkay() {
        val code = """
            every(100ms) {
                while(true) {
                    while(false) {
                        continue
                    }
                    continue
                    continue
                }
                while(false) {
                    continue
                }
            }
        """

        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(code)
        ContextualConstraintAnalyzer(ErrorHandler(code), st).caseStart(start)
    }

    @Test
    fun breakOutsideLoopThrowsException() {
        val code =
        """
            every(100ms) {
                while(true) {
                    while(false) {}
                    continue
                }
                break
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(code)
        assertThrows<LoopJumpOutOfLoopError> { ContextualConstraintAnalyzer(ErrorHandler(code), st).caseStart(start) }
    }

    @Test
    fun singleInitIsOkay(){
        val input = """
            init{
            
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start)
    }

    @Test
    fun multipleInitsThrowsException(){
        val input = """
            init{
                
            }
            
            init{
            
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        assertThrows<MultipleInitsError> { ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start) }
    }

    @Test
    fun stopCannotBeUsedInsideFunction(){
        val input = """
            fun foo() {
                stop
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        assertThrows<ModuleStatementUsedInFunctionException> { ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start) }
    }

    @Test
    fun stopCanBeUsedInsideModule(){
        val input = """
            module foo {
                every (1s)
                    stop
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start)
    }

    @Test
    fun startCannotBeUsedInsideFunction(){
        val input = """
            fun foo() {
                start bar
            }
            
            module bar {
                every (1s)
                    Int b = 2
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        assertThrows<ModuleStatementUsedInFunctionException> { ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start) }
    }

    @Test
    fun startCanBeUsedInsideModule(){
        val input = """
            module foo {
                every (1s)
                    start foo
            }
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start)
    }

    @Test
    fun variableGetsMarkedInitialisedInDclIfIthasInitializer(){
        val input = """
            Int a = 3
        """
        val (st, start) = compileUpToContextualConstraintsAnalyzerFromString(input)
        ContextualConstraintAnalyzer(ErrorHandler(input), st).caseStart(start)
        assert(st.findVar("a")!!.isInitialised)
    }


    private fun compileUpToContextualConstraintsAnalyzerFromString(input:String):Pair<SymbolTable, Start> {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        val s = parser.parse()
        val st = SymbolTableBuilder(ErrorHandler(input)).buildSymbolTable(s)
        return Pair(st, s)
    }
}