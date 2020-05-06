package semantics.SymbolTable.errors

import CompileError

class FunctionIdentifierUsedAsVariableError(message: String): CompileError(message)