import sablecc.node.Token
import java.lang.Integer.max

abstract class CompileError(msg: String) : Exception(msg) {
    var errorMsg:String = ""
        private set

    fun setError(error:String) {errorMsg = error}
}

class ErrorHandler(sourceProgram: String) {
    private var errorMsg:String = ""
    private var lastLine:Int? = null
    private var lastPos:Int? = null

    companion object {
        const val codeLookbackLength = 3
    }

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
                    sourceLines.subList(max(lastLine!! - codeLookbackLength, 0), lastLine!!).filter { o -> !o.all { it == '\r' || it == '\n' || it == ' ' || it == '\t' } }.joinToString("\n") + "\n" +
                    " ".repeat(lastPos!! - 1) + "^\n"
        }
        errorMsg += ce.message

        ce.setError(errorMsg)
        throw ce
    }
}