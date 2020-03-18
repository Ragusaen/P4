package semantics

import PrettyPrinter
import org.junit.jupiter.api.Test
import java.io.PushbackReader
import sablecc.lexer.Lexer
import sablecc.parser.Parser


internal class TypeCheckerTest {
    @Test
    fun plusAdditionIsTypeCorrectForTwoIntegers(){
        val input = "Int a = 5 + 8;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val a = parser.parse()


    }

    @Test
    @Suppress("UNREACHABLE_CODE")
    fun AssigningFloatToIntVarThrowsError(){
        val input = "Int a = 4.5;"
        val lexer = Lexer(PushbackReader(input.reader()))
        val parser = Parser(lexer)
        val a = parser.parse()




    }
}