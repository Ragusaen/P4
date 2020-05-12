package semantics.contextualConstraints

import CompileError
import ErrorHandler
import sablecc.node.*
import semantics.contextualConstraints.errors.LoopJumpOutOfLoopError
import semantics.contextualConstraints.errors.MultipleInitsError
import semantics.contextualConstraints.errors.ModuleStatementUsedInFunctionException
import semantics.contextualConstraints.errors.ReturnOutOfFunctionDeclarationError
import semantics.symbolTable.ScopedTraverser
import semantics.symbolTable.SymbolTable
import semantics.typeChecking.errors.IdentifierUsedBeforeAssignmentError

class ContextualConstraintAnalyzer(symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {
    private var openLoops: Int = 0
    private var inFunction = false
    private var initCount: Int = 0

    // Error handling
    private val errorHandler = ErrorHandler()
    private fun error(ce: CompileError):Nothing = errorHandler.compileError(ce)

    fun run(node: Start) {
        caseStart(node)
    }

    override fun outADelayStmt(node: ADelayStmt) {
        if (inFunction) {
            errorHandler.setLineAndPos(node.token)
            error(ModuleStatementUsedInFunctionException("Delay can only be used inside module declaration, not in function."))
        }
    }

    override fun outADelayuntilStmt(node: ADelayuntilStmt) {
        if (inFunction) {
            errorHandler.setLineAndPos(node.token)
            error(ModuleStatementUsedInFunctionException("Delay until can only be used inside module declaration, not in function."))
        }
    }

    override fun outAStopStmt(node: AStopStmt) {
        if (inFunction) {
            errorHandler.setLineAndPos(node.token)
            error(ModuleStatementUsedInFunctionException("Stop can only be used inside module declaration, not in function."))
        }
    }

    override fun outAStartStmt(node: AStartStmt) {
        if (inFunction) {
            errorHandler.setLineAndPos(node.token)
            error(ModuleStatementUsedInFunctionException("Stop can only be used inside module declaration, not in function."))
        }
    }

    override fun outABreakStmt(node: ABreakStmt) {
        errorHandler.setLineAndPos(node.token)

        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to break but was not inside of loop."))
    }

    override fun outAContinueStmt(node: AContinueStmt) {
        errorHandler.setLineAndPos(node.token)

        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to continue but was not inside of loop."))
    }

    override fun outAReturnStmt(node: AReturnStmt) {
        errorHandler.setLineAndPos(node.token)

        if (!inFunction)
            error(ReturnOutOfFunctionDeclarationError("Attempt to return from function, but was not inside of a function declaration."))
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

    override fun inAInitRootElement(node: AInitRootElement) {
        super.inAInitRootElement(node)
        errorHandler.setLineAndPos(node.token)

        initCount++
        if(initCount > 1)
            error(MultipleInitsError("Multiple init structures are declared."))
    }
}