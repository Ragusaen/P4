package semantics.SymbolTable

import sablecc.node.Node
import semantics.TypeChecking.Type

class Identifier(val type: Type, var isInitialised: Boolean = false) {
}


class TemplateModuleIdentifier(val paramTypes: List<Type>)
