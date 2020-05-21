import codegeneration.CodeGenerator
import sablecc.parser.Parser
import sablecc.parser.ParserException
import semantics.contextualConstraints.ContextualConstraintAnalyzer
import semantics.symbolTable.SymbolTableBuilder
import semantics.typeChecking.TypeChecker


fun main() {
    var input = """
Int a b = 3
"""
    input += "\n"

    try {
        val errorHandler = ErrorHandler(input)

        val lexer = StringLexer(input)

        val parser = Parser(lexer)

        val startNode = try {
            parser.parse()
        } catch (e: ParserException) {
            errorHandler.setLineAndPos(e.token)
            val msg = e.message?.replace("eol", "end of line")
            errorHandler.compileError(SableCCException(msg ?: "No message provided by SableCC"))
        }

        val st = SymbolTableBuilder(errorHandler).buildSymbolTable(startNode)
        ContextualConstraintAnalyzer(errorHandler, st).run(startNode)
        val tt = TypeChecker(errorHandler, st).run(startNode)
        val cg = CodeGenerator(tt, errorHandler, st)

        println(cg.generate(startNode))
    }
    catch (ce: CompileError) {
        println("Compilation stopped due to compile error.")
        println(ce.errorMsg)
    }
}

fun formatToSabbleCC(lines: List<String>) {
    val match = "\"([^)]*)\"".toRegex()
    val namematch = "[.]([^)]*)[)]".toRegex()

    for (l in lines) {
        val m = match.find(l)
        val n = namematch.find(l)
        if (m != null && n != null) {
            val mk = m.groupValues[1]
            val nk = n.groupValues[1].toLowerCase()
            println("$nk = '$mk';")
        }
    }
}