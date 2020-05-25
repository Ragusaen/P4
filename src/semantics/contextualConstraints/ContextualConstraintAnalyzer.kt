package semantics.contextualConstraints

import ErrorHandler
import sablecc.node.*
import semantics.contextualConstraints.errors.LoopJumpOutOfLoopError
import semantics.contextualConstraints.errors.MultipleInitsError
import semantics.contextualConstraints.errors.ModuleStatementUsedInFunctionError
import semantics.contextualConstraints.errors.ReturnOutOfFunctionDeclarationError
import semantics.symbolTable.ScopedTraverser
import semantics.symbolTable.SymbolTable
import semantics.typeChecking.errors.IdentifierUsedBeforeAssignmentError

class ContextualConstraintAnalyzer(errorHandler: ErrorHandler, symbolTable: SymbolTable) : ScopedTraverser(errorHandler, symbolTable) {
    private var openLoops: Int = 0
    private var inFunction = false
    private var inModule = false
    private var initHasAppeared = false

    fun run(node: Start) {
        caseStart(node)
    }

    override fun outADelayStmt(node: ADelayStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Delay can only be used inside module declaration."))
    }

    override fun outADelayuntilStmt(node: ADelayuntilStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Delay until can only be used inside module declaration."))
    }

    override fun outAStopStmt(node: AStopStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Stop can only be used inside module declaration."))
    }

    override fun outAStartStmt(node: AStartStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Stop can only be used inside module declaration."))
    }

    override fun outABreakStmt(node: ABreakStmt) {
        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to break but was not inside of loop."))
    }

    override fun outAContinueStmt(node: AContinueStmt) {
        if (openLoops <= 0)
            error(LoopJumpOutOfLoopError("Attempted to continue but was not inside of loop."))
    }

    override fun outAReturnStmt(node: AReturnStmt) {
        if (!inFunction)
            error(ReturnOutOfFunctionDeclarationError("Attempt to return from function, but was not inside of a function declaration."))
    }

    override fun outAVardcl(node: AVardcl) {
        val identifier = symbolTable.findVar(node.identifier.text)!!
        if (node.expr != null) {
            identifier.isInitialized = true
        }
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        symbolTable.findVar(node.identifier.text)!!.isInitialized = true
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val identifier = symbolTable.findVar(node.identifier.text)
        if (!identifier!!.isInitialized)
            error(IdentifierUsedBeforeAssignmentError("The variable ${node.identifier.text} was used before being initialized."))
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
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

    override fun outAWhileStmt(node: AWhileStmt) {
        super.outAWhileStmt(node)
        openLoops--
    }

    override fun inATemplateModuledcl(node: ATemplateModuledcl) {
        super.inATemplateModuledcl(node)
        inModule = true
    }

    override fun outATemplateModuledcl(node: ATemplateModuledcl) {
        super.outATemplateModuledcl(node)
        inModule = false
    }

    override fun inAInstanceModuledcl(node: AInstanceModuledcl) {
        super.inAInstanceModuledcl(node)
        inModule = true
    }

    override fun outAInstanceModuledcl(node: AInstanceModuledcl) {
        super.outAInstanceModuledcl(node)
        inModule = false
    }

    override fun outAInitRootElement(node: AInitRootElement) {
        super.inAInitRootElement(node)

        if(initHasAppeared)
            error(MultipleInitsError("Multiple init structures are declared."))
        initHasAppeared = true
    }
}