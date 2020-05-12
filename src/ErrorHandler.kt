import sablecc.node.Token

abstract class CompileError(msg: String) : Exception(msg) {
    var errorMsg:String = ""
        private set

    fun setError(error:String) {errorMsg = error}
}

class ErrorHandler {
    private var errorMsg:String = ""
    private var lastLine:Int? = null
    private var lastPos:Int? = null

    fun setLineAndPos(t:Token) {
        lastLine = t.line
        lastPos = t.pos
    }

    fun compileError(ce:CompileError):Nothing {
        if (lastLine == null && lastPos == null)
            errorMsg = "Line and position unavailable.\n"
        else
            errorMsg = "ERROR [$lastLine, $lastPos]\n"
        errorMsg += ce.message

        ce.setError(errorMsg)
        throw ce
    }
}