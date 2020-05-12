package semantics.typeChecking.errors

import CompileError

class IdentifierNotDeclaredError(message: String) : CompileError(message)