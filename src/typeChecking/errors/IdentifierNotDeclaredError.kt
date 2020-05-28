package typeChecking.errors

import CompileError

class IdentifierNotDeclaredError(message: String) : CompileError(message)