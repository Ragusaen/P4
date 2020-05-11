package semantics

import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import sablecc.parser.Parser
import semantics.ContextualConstraints.ContextualConstraintAnalyzer
import semantics.ContextualConstraints.Exceptions.LoopJumpOutOfLoopError
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationError
import semantics.SymbolTable.SymbolTable
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker

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
        assertThrows<ReturnOutOfFunctionDeclarationError> { ContextualConstraintAnalyzer(st).caseStart(start) }
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
        ContextualConstraintAnalyzer(st).caseStart(start)
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
        ContextualConstraintAnalyzer(st).caseStart(start)
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
        assertThrows<LoopJumpOutOfLoopError> { ContextualConstraintAnalyzer(st).caseStart(start) }
    }


    private fun compileUpToContextualConstraintsAnalyzerFromString(input:String):Pair<SymbolTable, Start> {
        val newInput = (input + "\n").replace("(?m)^[ \t]*\r?\n".toRegex(), "")
        val lexer = StringLexer(newInput)
        val parser = Parser(lexer)
        val s = parser.parse()
        val st = SymbolTableBuilder().buildSymbolTable(s)
        return Pair(st, s)
    }
}