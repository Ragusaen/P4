package semantics.symbolTable.errors

import CompileError

class FunctionIdentifierUsedAsVariableError(message: String): CompileError(message)