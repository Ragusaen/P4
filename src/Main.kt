import sablecc.lexer.Lexer
import sablecc.node.AAdditionBinop
import sablecc.node.ABinopExpr
import sablecc.node.ASubtractionBinop
import sablecc.node.PBinop
import sablecc.parser.Parser
import semantics.ScopePrinter
import semantics.ScopedTraverser
import semantics.SymbolTableBuilder
import semantics.Type
import java.io.PushbackReader
import kotlin.reflect.typeOf


fun main() {
    val input = """
        Int i = 5;

        template module thismodule {
            every (1000) {
                i += 1;
                start(blink1);
                delay(500ms);
                stop;
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