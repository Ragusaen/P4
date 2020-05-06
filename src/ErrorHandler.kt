import sablecc.node.Token

abstract class CompileError(msg: String) : Exception(msg)

class ErrorHandler {
    private var lastToken:Token? = null
    private var lastLine:Int? = null
    private var lastPos:Int? = null

    fun setLineAndPos(t:Token) {
        lastLine = t.line
        lastPos = t.pos
    }

    fun setLineAndPos(line:Int, pos:Int) {
        lastLine = line
        lastPos = pos
    }

    fun compileError(ce:CompileError, t:Token? = null):Nothing {
        if (t != null)
            setLineAndPos(t)

        print("ERROR" + if(lastLine != null) ": Line $lastLine, Pos $lastPos" else { "" } + "\n")
        printExceptionAndThrow(ce)
    }

    private fun printExceptionAndThrow(ce: CompileError):Nothing {
        println(ce.message)
        throw ce
    }
}