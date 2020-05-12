package semantics.symbolTable.errors

import CompileError

class IdentifierUsedBeforeDeclarationError(message:String) : CompileError(message)