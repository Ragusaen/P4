package semantics

import StringLexer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sablecc.node.Start
import sablecc.parser.Parser
import semantics.ContextualConstraints.ContextualConstraintAnalyzer
import semantics.ContextualConstraints.Exceptions.LoopJumpOutOfLoopError
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationError

internal class ContextualConstraintsTest {
    private fun parse(input: String): Start {
        return Parser(StringLexer(input)).parse()
    }

    @Test
    fun returnOutOfFunctionDeclarationThrowsException() {
        val s = parse("""
            every (100) {
                return;
            }
        """)

        assertThrows<ReturnOutOfFunctionDeclarationError>{ ContextualConstraintAnalyzer().caseStart(s) }
    }

    @Test
    fun returnInsideFunctionDeclarationIsOkay() {
        val s = parse("""
            fun foo() {
                return;
            }
        """)

        ContextualConstraintAnalyzer().caseStart(s)
    }

    @Test
    fun breakAndContinueInsideNestedLoopsIsOkay() {
        val s = parse("""
            every(100) {
                while(true) {
                    while(false) {
                        continue;
                        for (Int i = 0; i < 2; i += 1) {
                            break;
                        }
                    }
                    continue;
                    continue;
                }
                while(false) {
                    continue;
                    for (Int i = 0; i < 2; i += 1) {
                        break;
                    }
                }
            }
        """)

        ContextualConstraintAnalyzer().caseStart(s)
    }

    @Test
    fun breakOutsideLoopThrowsException() {
        val s = parse("""
            every(100) {
                while(true) {
                    while(false) {}
                    continue;
                }
                break;
            }
        """)

        assertThrows<LoopJumpOutOfLoopError> { ContextualConstraintAnalyzer().caseStart(s) }
    }
}