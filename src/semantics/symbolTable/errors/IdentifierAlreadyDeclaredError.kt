package semantics.symbolTable.errors

import CompileError

class IdentifierAlreadyDeclaredError(message:String) : CompileError(message)