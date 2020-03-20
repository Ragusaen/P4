import sablecc.lexer.Lexer
import java.io.PushbackReader

class StringLexer(input:String, size:Int = 1024) : Lexer(PushbackReader(input.reader(), size))
