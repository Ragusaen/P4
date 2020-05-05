package semantics.TypeChecking.Exceptions

import CompileError

class IdentifierNotDeclaredException(message: String) : CompileError(message)