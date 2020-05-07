import codegeneration.CodeGenerator
import sablecc.parser.Parser
import semantics.ContextualConstraints.ContextualConstraintAnalyzer
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker
import java.lang.Exception


fun main() {
    val input =
"""
    template module a(Int a) {
            String s = "hey";
            every(100ms) {
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
        }

"""
    try {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)

        val a = parser.parse()
        val st = SymbolTableBuilder().buildSymbolTable(a)
        ContextualConstraintAnalyzer(st).run(a)
        val tt = TypeChecker(st).run(a)
        val cg = CodeGenerator(tt, st)

        println(cg.generate(a))
    }
    catch (ce:CompileError) {
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