package semantics.ContextualConstraints

import CompileError
import ErrorHandler
import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import semantics.ContextualConstraints.Exceptions.LoopJumpOutOfLoopError
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationError
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable
import semantics.TypeChecking.Type
import semantics.TypeChecking.errors.IdentifierUsedBeforeAssignmentError

class ContextualConstraintAnalyzer(symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {
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

    override fun outAVardcl(node: AVardcl) {
        val identifier = symbolTable.findVar(node.identifier.text)!!
        if (node.expr != null) {
            identifier.isInitialised = true
        }
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        symbolTable.findVar(node.identifier.text)!!.isInitialised = true
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        errorHandler.setLineAndPos(node.identifier)

        val identifier = symbolTable.findVar(node.identifier.text)
        if (!identifier!!.isInitialised)
            error(IdentifierUsedBeforeAssignmentError("The variable ${node.identifier.text} was used before being initialized."))
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        errorHandler.setLineAndPos(node.identifier)
        super.inAFunctiondcl(node)
        inFunction = true
    }

    override fun outAFunctiondcl(node: AFunctiondcl) {
        super.outAFunctiondcl(node)
        inFunction = false
    }

    override fun inAForStmt(node: AForStmt) {
        super.inAForStmt(node)
        openLoops++
    }

    override fun outAForStmt(node: AForStmt) {
        super.outAForStmt(node)
        openLoops--
    }

    override fun inAWhileStmt(node: AWhileStmt) {
        super.inAWhileStmt(node)
        openLoops++
    }

    override fun outAWhileStmt(node: AWhileStmt?) {
        super.outAWhileStmt(node)
        openLoops--
    }
}