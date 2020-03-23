package parser

import StringLexer
import sablecc.node.Start
import sablecc.parser.Parser

internal class ParsingTests {



    fun parseString(input:String): Start {
        val lexer = StringLexer(input)
        val parser = Parser(lexer)
        return parser.parse()
    }
}