import sablecc.node.*
import semantics.typeChecking.Type

class Helper {
    companion object {
        fun getTypeFromPType(node: PType): Type {
            return when(node) {
                is AIntType -> Type.Int
                is AFloatType -> Type.Float
                is AStringType -> Type.String
                is ABoolType -> Type.Bool
                is ADigitalinputpinType -> Type.DigitalInputPin
                is ADigitaloutputpinType -> Type.DigitalOutputPin
                is AAnaloginputpinType -> Type.AnalogInputPin
                is AAnalogoutputpinType -> Type.AnalogOutputPin
                is ATimeType -> Type.Time
                is AArrayType -> Type.createArrayOf(getTypeFromPType(node.type))
                else -> throw Exception("Unsupported node type")
            }
        }

        fun getFunParams(node:AFunctiondcl): List<Type> {
            return node.param.map { getTypeFromPType((it as AParam).type) }
        }
    }
}