import codegeneration.CodeGenerator
import sablecc.parser.Parser
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker


fun main() {
    val input = """
        Int a = 5;
        Int b = -5;
        Float c = 3.5;
        Float d = -3.5;
        Float e = .3;
        Float f = 5.;
        Time g = 6s;
        Time h = 5.h;
        Time i = .6m;
        Time j = 5.6ms;
        Time k = -5ms;
    """

    val lexer = StringLexer(input)
    val parser = Parser(lexer)

    val a = parser.parse()

    val st = SymbolTableBuilder().buildSymbolTable(a)
    val tt = TypeChecker(st).run(a)

    val cg = CodeGenerator(tt, st)
    println(cg.generate(a))
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