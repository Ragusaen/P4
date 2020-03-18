package semantics

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

class SymbolTableBuilder : DepthFirstAdapter() {
    private val st = SymbolTable()

    fun buildSymbolTable(s: Start):SymbolTable {
        caseStart(s)
        return st
    }

    override fun outAVardcl(node: AVardcl) {
        val name = node.identifier.text
        val type = (node.parent() as ADclStmt).type.toString()

        try {
            st.add(name, Identifier(type, null, node))
        }
        catch (e:IdentifierAlreadyDeclaredException) {
            throw e
            // todo "Append to error list"
        }
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val name = node.identifier.text

        try {
            st.find(name)
        }
        catch (e:IdentifierUsedBeforeDeclarationException) {
            throw e
        }
    }
}
