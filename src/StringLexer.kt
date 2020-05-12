import sablecc.lexer.Lexer
import java.io.PushbackReader

class StringLexer(input:String, size:Int = 1024) : DumplingLexer(PushbackReader(input.reader(), size))
