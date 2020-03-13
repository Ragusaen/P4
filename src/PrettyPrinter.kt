
import sablecc.analysis.Analysis
import sablecc.analysis.DepthFirstAdapter
import sablecc.node.AValueExpr
import sablecc.node.Node
import sablecc.node.PValue
import sablecc.node.Start

class PrettyPrinter : DepthFirstAdapter() {
    override fun defaultIn(node: Node?) {
        if (node != null) {
            if (node is AValueExpr) {
                print("${node.value} ")
            } else if (node !is PValue) {
                print("${node.javaClass.simpleName}(")
            }
        }
    }

    override fun defaultOut(node: Node?) {
        if (node != null) {
            if (node !is AValueExpr && node !is PValue) {
                print(") ")
            }
        }
    }

    fun print(startNode: Start) {
        caseStart(startNode)
    }
}