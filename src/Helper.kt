import sablecc.node.*
import semantics.typeChecking.Type

class Helper {
    companion object {
        fun getTypeFromPType(node: PType): Type {
            return when(node) {
                is AIntType -> Type.Int
                is AInt8Type -> Type.Int8
                is AInt16Type -> Type.Int16
                is AInt32Type -> Type.Int32
                is AInt64Type -> Type.Int64
                is AFloatType -> Type.Float
                is AFloat32Type -> Type.Float32
                is AFloat64Type -> Type.Float64
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