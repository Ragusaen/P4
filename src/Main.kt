import sablecc.lexer.Lexer
import sablecc.parser.Parser
import java.io.PushbackReader


fun main() {
    val input = """
        Int a = 0;
        
        template module thismodule {
            Int a = 3;
            Float k = 3.5;
            
            every (1000) {
                ; 
            }
        }
    """


    val lexer = Lexer(PushbackReader(input.reader(), 1024))
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