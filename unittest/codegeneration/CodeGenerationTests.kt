package codegeneration

import ErrorHandler
import StringLexer
import org.junit.jupiter.api.Test
import sablecc.parser.Parser
import semantics.contextualConstraints.ContextualConstraintAnalyzer
import semantics.symbolTable.SymbolTableBuilder
import semantics.typeChecking.TypeChecker

internal class CodeGenerationTestsTest {
    @Test
    fun callingFunctionInsideTemplateModuleCanCompile() {
        val code = """
            fun foo():Int {
                return 0
            }

            template module a {
                every(1000ms) {
                    Int a = foo()
                }
            }
        """

        compile(code)
    }

    @Test
    fun callingFunctionInsideAnonymousModuleCanCompile() {
        val code = """
            fun foo():Int {
                return 0
            }
            
            every(1000ms) {
                Int a = foo()
            }
        """

        compile(code)
    }

    @Test
    fun callingFunctionToInitialiseGlobalVariableCanCompile() {
        val code = """
            Int a = foo()
            
            fun foo():Int {
                return 0
            }
        """

        compile(code)
    }

    fun compile(code:String) {
        val errorHandler = ErrorHandler(code)
        val lexer = StringLexer(code)
        val parser = Parser(lexer)
        val a = parser.parse()
        val st = SymbolTableBuilder(errorHandler).buildSymbolTable(a)
        ContextualConstraintAnalyzer(errorHandler, st).run(a)
        val tt = TypeChecker(errorHandler, st).run(a)
        val cg = CodeGenerator(tt, errorHandler, st)
        cg.generate(a)
    }
}

