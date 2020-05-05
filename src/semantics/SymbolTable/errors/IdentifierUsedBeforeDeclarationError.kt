package semantics.SymbolTable.errors

import CompileError

class IdentifierUsedBeforeDeclarationError(message:String) : CompileError(message)