package contextualConstraints.errors

import CompileError

class ModuleStatementUsedInFunctionError(msg: String) : CompileError(msg)