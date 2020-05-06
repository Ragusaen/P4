import sablecc.node.Token

abstract class CompileError(msg: String) : Exception(msg)

class ErrorHandler {
    private var lastLine:Int? = null
    private var lastPos:Int? = null
    private var lastToken:Token? = null

    fun setLineAndPos(t:Token) {
        lastLine = t.line
        lastPos = t.pos
        lastToken = t // debugging
    }

    fun compileError(ce:CompileError):Nothing {
        if (lastLine == null && lastPos == null)
            println("Line and position unavailable.")

        print("ERROR" + if(lastLine != null) ": Line $lastLine, Pos $lastPos" else "")
        if (lastToken != null)
            println(" " + lastToken!!.javaClass.canonicalName)
        else
            println()
        printExceptionAndThrow(ce)
    }

    private fun printExceptionAndThrow(ce: CompileError):Nothing {
        println(ce.message)
        throw ce
    }
}