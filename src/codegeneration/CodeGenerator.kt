package codegeneration

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

class CodeGenerator : DepthFirstAdapter() {

    private var emittedCode = ""
    private fun emit(code: String) {
        emittedCode += code
    }

    fun generate(startNode: Start): String {
        caseStart(startNode)
        return emittedCode
    }


    override fun caseAIfStmt(node: AIfStmt) {
        emit("if (")
        node.expr.apply(this)
        emit(") ")
        node.ifBody.apply(this)
        if (node.elseBody != null) {
            emit("else")
            node.elseBody.apply(this)
        }
    }
}