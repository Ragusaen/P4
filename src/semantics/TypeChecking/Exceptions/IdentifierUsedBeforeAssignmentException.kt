package semantics.TypeChecking.Exceptions

import CompileError

class IdentifierUsedBeforeAssignmentException(message:String) : CompileError(message)
