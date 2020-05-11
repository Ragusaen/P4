package semantics.SymbolTable.errors

import CompileError

class VariableIdentifierUsedAsFunctionError(message: String) : CompileError(message)