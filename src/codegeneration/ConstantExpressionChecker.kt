package codegeneration

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.AFunctionCallExpr
import sablecc.node.AIdentifierValue

class ConstantExpressionChecker : DepthFirstAdapter() {
    var isConstant = true

    override fun outAIdentifierValue(node: AIdentifierValue) {
        isConstant = false
    }

    override fun outAFunctionCallExpr(node: AFunctionCallExpr) {
        isConstant = false
    }
}