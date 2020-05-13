package codegeneration

open class ModuleAux(val name: String,
                     val expr: String,
                     val isEveryStruct: Boolean
)

class TemplateModuleAux(val dclInits: List<String>, val structDefinition: String, name: String, expr: String, isEveryStruct: Boolean) : ModuleAux(name, expr, isEveryStruct) {

}

data class TemplateInstance(val name: String, val arguments: List<String>)