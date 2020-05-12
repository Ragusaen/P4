package semantics.typeChecking.errors

import CompileError

class IdentifierUsedBeforeAssignmentError(message:String) : CompileError(message)
