import codegeneration.CodeGenerator
import sablecc.lexer.LexerException
import sablecc.node.TWhitespace
import sablecc.parser.Parser
import sablecc.parser.ParserException
import contextualConstraints.ContextualConstraintAnalyzer
import symboltable.SymbolTableBuilder
import typeChecking.TypeChecker
import java.io.Reader

class DumplingCompiler {

    fun compile(reader: Reader): String? {
        val input = reader.readText()
        try {
            val errorHandler = ErrorHandler(input)

            val lexer = StringLexer(input)
            val parser = Parser(lexer)

            val startNode = try {
                parser.parse()
            } catch (e: ParserException) {
                errorHandler.setLineAndPos(e.token)
                val msg = e.message?.replace("eol", "end of line")
                if (msg != null) {
                    val nmsg = msg.replace("""\[(\d+),(\d+)]""".toRegex(), "")
                    errorHandler.compileError(SableCCException("This token cannot be placed here. $nmsg"))
                } else {
                    errorHandler.compileError(SableCCException("Sorry, no message to display"))
                }
            } catch (e: LexerException) {
                val msg = e.message

                if (msg != null) {
                    // Grab line and column from the string because the LexerException cannot pass along a token for it
                    val match = """\[(\d+),(\d+)]""".toRegex().find(msg)?.groupValues

                    if (match != null) {
                        val (line, column) = match.drop(1).map {Integer.parseInt(it)}

                        // Create a fake token to pass to the error handler
                        val fakeToken = TWhitespace("FakeToken")
                        fakeToken.line = line
                        fakeToken.pos = column
                        errorHandler.setLineAndPos(fakeToken)
                        val nmsg = msg.replace("""\[(\d+),(\d+)\]""".toRegex(), "")
                        errorHandler.compileError(SableCCException("This symbol does not exist in Dumpling. $nmsg"))
                    } else {
                        errorHandler.compileError(SableCCException(msg))
                    }

                } else
                    errorHandler.compileError(SableCCException("No message provided by SableCC"))
            }

            val st = SymbolTableBuilder(errorHandler).buildSymbolTable(startNode)
            ContextualConstraintAnalyzer(errorHandler, st).run(startNode)
            val tt = TypeChecker(errorHandler, st).run(startNode)
            val cg = CodeGenerator(tt, errorHandler, st)

            return cg.generate(startNode)
        }
        catch (ce: CompileError) {
            println("Compilation stopped due to compile error.")
            println(ce.errorMsg)
        }
        return null
    }
}