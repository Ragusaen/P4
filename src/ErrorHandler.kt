import sablecc.node.Token
import java.lang.Integer.max

class SableCCException(msg: String) : CompileError(msg)

data class ErrorOtherPoint(val line: Int, val column: Int, val msg: String)

fun getOtherPointFromToken(token: Token, msg: String? = null): ErrorOtherPoint {
    return ErrorOtherPoint(token.line, token.pos, msg ?: "")
}

abstract class CompileError(msg: String, val otherPoint: ErrorOtherPoint? = null) : Exception(msg) {
    var errorMsg:String = ""
        private set

    fun setError(error:String) {errorMsg = error}
}

class ErrorHandler(sourceProgram: String) {
    private var errorMsg:String = ""
    private var lastLine: Int? = null
    private var lastPos: Int? = null
    var lastToken: Token? = null

    companion object {
        const val codeLookbackLength = 3
    }

    val sourceLines = sourceProgram.split('\n')

    fun setLineAndPos(t:Token) {
        lastToken = t
        lastLine = t.line
        lastPos = t.pos
    }

    fun compileError(ce:CompileError):Nothing {
        if (lastLine == null || lastPos == null)
            errorMsg = "Line and position unavailable.\n"
        else {
            errorMsg = "On line $lastLine at column $lastPos\n" + generateLookBack()
        }
        errorMsg += ce.message

        val otherPoint = ce.otherPoint
        if (otherPoint != null) {
            errorMsg += "\n" + generateLookBack(otherPoint.line, otherPoint.column) + otherPoint.msg
        }


        ce.setError(errorMsg)
        throw ce
    }

    private fun generateLookBack(line: Int = lastLine!!, column: Int = lastPos!!): String {
        // Grab the codeLookbackLength last lines
        return sourceLines.withIndex().toList().subList(max(line - codeLookbackLength, 0), line)
                // Drop all initial lines that are just whitespace
                .dropWhile {it.value.all { it == '\r' || it == '\n' || it == ' ' || it == '\t' }}
                // Add their line numbers
                .map { "${it.index + 1}: ${it.value}"}
                // Join them together with newlines
                .joinToString ("\n") + "\n" +
                // Make whitespace such that the ^ hits the correct symbol.
                " ".repeat(column - 1 + ("$line".length + 2)) + "^\n"
    }
}