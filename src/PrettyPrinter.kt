
import sablecc.analysis.Analysis
import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

class PrettyPrinter : DepthFirstAdapter() {
    override fun defaultIn(node: Node?) {
        if (node != null) {
            print("${node.javaClass.simpleName}(")
            if (node is AVardcl) {
                print(" ${node.identifier} ")
            }
        }
    }

    override fun defaultOut(node: Node?) {
        if (node != null) {
            print(") ")
        }
    }

    fun print(startNode: Start) {
        caseStart(startNode)
    }
}