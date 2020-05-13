package semantics.contextualConstraints.errors

import CompileError
import java.lang.Exception

class ModuleStatementUsedInFunctionException(msg: String) : CompileError(msg) {

}