package semantics.SymbolTable.errors

import CompileError

class IdentifierAlreadyDeclaredError(message:String) : CompileError(message)