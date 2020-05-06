package semantics.TypeChecking.errors

import CompileError

class IdentifierUsedBeforeAssignmentError(message:String) : CompileError(message)
