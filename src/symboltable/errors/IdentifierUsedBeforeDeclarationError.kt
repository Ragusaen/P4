package symboltable.errors

import CompileError

class IdentifierUsedBeforeDeclarationError(message:String) : CompileError(message)