import sablecc.lexer.Lexer
import sablecc.parser.Parser
import java.io.PushbackReader


fun main() {
    val input = "Int variable = 3 * (6 + 4), othervar;"
    val lexer = Lexer(PushbackReader(input.reader()))
    val parser = Parser(lexer)

    val a = parser.parse()

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