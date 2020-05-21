package semantics.symbolTable.errors

import CompileError
import ErrorOtherPoint

class IdentifierAlreadyDeclaredError(message:String, otherPoint: ErrorOtherPoint? = null) : CompileError(message, otherPoint)