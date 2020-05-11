import codegeneration.CodeGenerator
import sablecc.parser.Parser
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker
import java.lang.Exception


fun main() {
    var input = """
Bool state = LOW

Bool b = HIGH
    """
    input = input.replace("(?m)^[ \t]*\r?\n".toRegex(), "")
    try {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val a = parser.parse()

        val st = SymbolTableBuilder().buildSymbolTable(a)
        val tt = TypeChecker(st).run(a)

        val cg = CodeGenerator(tt, st)
        println(cg.generate(a))
    }
    catch (ce:CompileError) {
        println("Compilation stopped due to compile error.")
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