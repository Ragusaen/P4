package semantics.contextualConstraints

import ErrorHandler
import sablecc.node.*
import semantics.contextualConstraints.errors.*
import semantics.symbolTable.ScopedTraverser
import semantics.symbolTable.SymbolTable
import semantics.typeChecking.errors.IdentifierUsedBeforeAssignmentError

class ContextualConstraintAnalyzer(errorHandler: ErrorHandler, symbolTable: SymbolTable) : ScopedTraverser(errorHandler, symbolTable) {
    private var openLoops: Int = 0
    private var inFunction = false
    private var inModule = false
    private var initHasAppeared = false
    private var inCriticalSection = false

    fun run(node: Start) {
        caseStart(node)
    }

    override fun outAUsleepStmt(node: AUsleepStmt) {
        if (!inCriticalSection)
            error(CriticalSectionError("Micro-sleep control structure cannot be used outside of critical section."))
    }

    override fun outASleepStmt(node: ASleepStmt) {
        if (!inCriticalSection)
            error(CriticalSectionError("Sleep control structure cannot be used outside of critical section."))
    }

    override fun outADelayStmt(node: ADelayStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Delay can only be used inside module declaration."))
        if(inCriticalSection)
            error(CriticalSectionError("Delay Statement cannot be used in critical control structure."))
    }

    override fun outADelayuntilStmt(node: ADelayuntilStmt) {
        if (!inModule)
            error(ModuleStatementUsedInFunctionError("Delay until can only be used inside module declaration."))
        if(inCriticalSection)
            error(CriticalSectionError("Delay Until Statement cannot be used in critical control structure."))
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

    override fun inACriticalStmt(node: ACriticalStmt) {
        if (inCriticalSection)
            error(CriticalSectionError("Cannot use critical control structure inside another critical control structure."))
        inCriticalSection = true
    }

    override fun outACriticalStmt(node: ACriticalStmt) {
        inCriticalSection = false
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