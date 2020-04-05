package semantics.ContextualConstraints

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import semantics.ContextualConstraints.Exceptions.LoopJumpOutOfLoopException
import semantics.ContextualConstraints.Exceptions.ReturnOutOfFunctionDeclarationException
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable

class ContextualConstraintAnalyzer : DepthFirstAdapter() {

    private var openLoops: Int = 0
    private var inFunction = false


    override fun outABreakStmt(node: ABreakStmt) {
        if (openLoops <= 0)
            throw LoopJumpOutOfLoopException("Attempt to break but was not inside of loop")
    }
    override fun outAContinueStmt(node: AContinueStmt) {
        if (openLoops <= 0)
            throw LoopJumpOutOfLoopException("Attempt to continue but was not inside of loop")
    }

    override fun outAReturnStmt(node: AReturnStmt) {
        if (!inFunction)
            throw ReturnOutOfFunctionDeclarationException("Attempt to return from function, but was not inside of a function declaration")
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