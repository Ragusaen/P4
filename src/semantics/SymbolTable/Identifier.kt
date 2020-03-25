package semantics.SymbolTable

import sablecc.node.Node
import semantics.TypeChecking.Type

class Identifier(val type: Type) {
    var isInitialised = false
}

class ModuleIdentifier(val paramTypes: List<Type>)
