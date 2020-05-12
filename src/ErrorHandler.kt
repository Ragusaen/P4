import sablecc.node.Token

abstract class CompileError(msg: String) : Exception(msg) {
    var errorMsg:String = ""
        private set

    fun setError(error:String) {errorMsg = error}
}

class ErrorHandler(sourceProgram: String) {
    private var errorMsg:String = ""
    private var lastLine:Int? = null
    private var lastPos:Int? = null

    val sourceLines = sourceProgram.split('\n')

    fun setLineAndPos(t:Token) {
        lastLine = t.line
        lastPos = t.pos
    }

    fun compileError(ce:CompileError):Nothing {
        if (lastLine == null || lastPos == null)
            errorMsg = "Line and position unavailable.\n"
        else {
            errorMsg = "ERROR [$lastLine, $lastPos]\n" +
                    sourceLines[lastLine!! - 1] + "\n" +
                    " ".repeat(lastPos!! - 1) + "^\n"
        }
        errorMsg += ce.message

        ce.setError(errorMsg)
        throw ce
    }
}