package semantics.SymbolTable

import sablecc.node.Node
import semantics.TypeChecking.Type

open class Identifier(val type: Type) {
    var isInitialised = false
}
