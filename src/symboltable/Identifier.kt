package symboltable

import sablecc.node.Token
import typeChecking.Type

class Identifier(val type: Type, val outName: String, val token: Token, var isInitialized: Boolean = false)

class TemplateModuleIdentifier(val paramTypes: List<Type>)
