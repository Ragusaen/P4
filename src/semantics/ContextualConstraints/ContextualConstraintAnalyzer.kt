package semantics.ContextualConstraints

import CompileError
import ErrorHandler
import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import semantics.ContextualConstraints.Exceptions.LoopJumpOutOfLoopError
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationError
import semantics.TypeChecking.Type

class ContextualConstraintAnalyzer : DepthFirstAdapter() {
    private var openLoops: Int = 0
    private var inFunction = false

    // Error handling
    private val errorHandler = ErrorHandler()
    private fun error(ce:CompileError):Nothing = errorHandler.compileError(ce)

    fun run(node: Start) {
        caseStart(node)
    }

    override fun outABreakStmt(node: ABreakStmt) {
        errorHandler.setLineAndPos(node.`break`)

        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to break but was not inside of loop"))
    }

    override fun outAContinueStmt(node: AContinueStmt) {
        errorHandler.setLineAndPos(node.`continue`)

        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to continue but was not inside of loop"))
    }

    override fun outAReturnStmt(node: AReturnStmt) {
        errorHandler.setLineAndPos(node.`return`)

        if (!inFunction)
            error(ReturnOutOfFunctionDeclarationError("Attempt to return from function, but was not inside of a function declaration"))
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        inFunction = true
    }

    override fun outAFunctiondcl(node: AFunctiondcl) {
        inFunction = false
    }

    override fun inAForStmt(node: AForStmt) {
        super.inAForStmt(node)
        openLoops++
    }

    override fun outAForStmt(node: AForStmt) {
        openLoops--
    }

    override fun inAWhileStmt(node: AWhileStmt) {
        openLoops++
    }

    override fun outAWhileStmt(node: AWhileStmt?) {
        openLoops--
    }
}