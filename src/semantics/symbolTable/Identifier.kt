package semantics.symbolTable

import sablecc.node.Token
import semantics.typeChecking.Type

class Identifier(val type: Type, val outName: String, val token: Token, var isInitialized: Boolean = false)

class TemplateModuleIdentifier(val paramTypes: List<Type>)
