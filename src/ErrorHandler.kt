import sablecc.node.Token

abstract class CompileError(msg: String) : Exception(msg)

class ErrorHandler {
    private var lastLine:Int? = null
    private var lastPos:Int? = null

    fun setLineAndPos(t:Token) {
        lastLine = t.line
        lastPos = t.pos
    }

    fun compileError(ce:CompileError):Nothing {
        if (lastLine == null && lastPos == null)
            println("Line and position unavailable.")

        println("ERROR" + if(lastLine != null) ": Line $lastLine, Pos $lastPos" else "")
        printExceptionAndThrow(ce)
    }

    private fun printExceptionAndThrow(ce: CompileError):Nothing {
        println(ce.message)
        throw ce
    }
}