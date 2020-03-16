package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

class SymbolTableBuilder : DepthFirstAdapter() {
    val st = SymbolTable()

    fun buildSymbolTable(s: Start) {
        processNode(s)
    }

    private fun processNode(n:Node) {

    }
}
