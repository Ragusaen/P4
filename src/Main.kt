import codegeneration.CodeGenerator
import sablecc.parser.Parser
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker


fun main() {
    val input = """
        Int a = 5;
        Int b = -5;
        Int c = +5;
        Float d = 3.5;
        Float e = -3.5;
        Float f = +3.5;
        Float g = .3;
        Float h = 5.;
        Time i = 6s;
        Time j = 5.h;
        Time k = .6m;
        Time l = 5.6ms;
        Time m = -5ms;
        Time n = +8m;
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