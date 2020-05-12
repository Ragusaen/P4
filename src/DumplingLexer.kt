import sablecc.lexer.Lexer
import sablecc.node.TEol
import sablecc.node.TWhitespace
import sablecc.node.Token
import java.io.PushbackReader

open class DumplingLexer(pbr: PushbackReader) : Lexer(pbr) {

    var lastNewLine = true

    override fun filter() {
        if (this.token is TEol) {
            if (lastNewLine) {
                this.token = null
            }
            lastNewLine = true
        } else if (this.token !is TWhitespace) {
            lastNewLine = false
        }
    }

}