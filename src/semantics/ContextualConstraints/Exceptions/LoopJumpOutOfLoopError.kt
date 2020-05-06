package semantics.ContextualConstraints.Exceptions

import CompileError

class LoopJumpOutOfLoopError(msg: String) : CompileError(msg)