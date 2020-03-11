import sablecc.analysis.DepthFirstAdapter
import sablecc.lexer.IPushbackReader
import sablecc.lexer.Lexer
import sablecc.node.AAdditionBinop
import sablecc.node.Switch
import sablecc.parser.Parser
import kotlin.reflect.typeOf


fun main() {
    val input = "7+8+2"
    val lexer = Lexer(reader(input.toCharArray()))
    val parser = Parser(lexer)

    val a = parser.parse()
    val dfa = DepthFirstAdapter(a)

    a.pExpr.apply(Switch)
}

class reader(val string: CharArray) : IPushbackReader {
    var index = 0

    override fun unread(c: Int) {
        string[--index] = c.toChar()
    }

    override fun read(): Int {
        if (index >= string.size)
            return -1
        return string[index++].toInt()
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