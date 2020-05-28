import sablecc.node.*
import typeChecking.Type

class Helper {
    companion object {
        fun getTypeFromPType(node: PType): Type {
            return when(node) {
                is AIntType -> Type.Int
                is AInt8Type -> Type.Int8
                is AInt16Type -> Type.Int16
                is AInt32Type -> Type.Int32
                is AInt64Type -> Type.Int64
                is AUintType -> Type.Uint
                is AUint8Type -> Type.Uint8
                is AUint16Type -> Type.Uint16
                is AUint32Type -> Type.Uint32
                is AUint64Type -> Type.Uint64
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


        fun lcs(x: String, y: String): String {
            if (x.isEmpty() || y.isEmpty()) return ""
            val x1 = x.dropLast(1)
            val y1 = y.dropLast(1)
            if (x.last() == y.last()) return lcs(x1, y1) + x.last()
            val x2 = lcs(x, y1)
            val y2 = lcs(x1, y)
            return if (x2.length > y2.length) x2 else y2
        }
    }
}

