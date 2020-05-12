package codegeneration

data class ModuleAux(val name: String,
                     val expr: String,
                     val isEveryStruct: Boolean,
                     val isTemplate: Boolean
)