import sablecc.lexer.Lexer
import sablecc.parser.Parser
import semantics.ScopePrinter
import semantics.ScopedTraverser
import semantics.SymbolTableBuilder
import java.io.PushbackReader


fun main() {
    val input = """
        Bool a = true;
        
        template module thismodule {
            every (1000ms) {
                if(a)
                    while(a) if(a) ; else ;
                delay until(a);
            }
        }
    """

    val lexer = StringLexer(input)
    val parser = Parser(lexer)

    val a = parser.parse()

    val scope = SymbolTableBuilder().buildSymbolTable(a)
    ScopedTraverser(scope).traverse(a)

    ScopePrinter(scope).traverse(a)

    PrettyPrinter().print(a)
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