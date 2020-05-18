package semantics.symbolTable

import semantics.typeChecking.Type

class Identifier(val type: Type, val outName: String, var isInitialized: Boolean = false)

class TemplateModuleIdentifier(val paramTypes: List<Type>)
