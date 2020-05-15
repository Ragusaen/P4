import codegeneration.CodeGenerator
import sablecc.node.Token
import sablecc.parser.Parser
import semantics.contextualConstraints.ContextualConstraintAnalyzer
import semantics.symbolTable.SymbolTableBuilder
import semantics.typeChecking.TypeChecker


fun main() {
    var input = """
every(1000ms) {
    delay 1000ms
}
"""
    input += "\n"

    try {
        val errorHandler = ErrorHandler(input)

        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val a = parser.parse()
        val st = SymbolTableBuilder(errorHandler).buildSymbolTable(a)
        ContextualConstraintAnalyzer(errorHandler, st).run(a)
        val tt = TypeChecker(errorHandler, st).run(a)
        val cg = CodeGenerator(tt, errorHandler, st)

        println(cg.generate(a))
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