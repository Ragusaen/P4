package codegeneration

open class ModuleAux(val name: String,
                     val expr: String,
                     val isEveryStruct: Boolean,
                     val prefixCode: String
)

class TemplateModuleAux(val dclInits: List<String>,
                        val structDefinition: String,
                        name: String, expr: String,
                        isEveryStruct: Boolean,
                        prefixCode: String) : ModuleAux(name, expr, isEveryStruct, prefixCode)

data class TemplateInstance(val name: String, val arguments: List<String>)