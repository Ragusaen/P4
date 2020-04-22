import codegeneration.CodeGenerator
import sablecc.parser.Parser
import semantics.SymbolTable.SymbolTableBuilder
import semantics.TypeChecking.TypeChecker


fun main() {
    val input = """
        Int a = 3, b, c = 2;
        Time h = 13h;
        
        fun foo(Int a, Int b): Int {
            return a * b + a / b - 3;
        }
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