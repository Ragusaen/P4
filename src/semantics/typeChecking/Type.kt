package semantics.typeChecking

import semantics.typeChecking.Type.Companion.DigitalPin

class Type private constructor(private val main: EType, private val subType: Type? = null) {
    private enum class EType {
        INT, INT8, INT16, INT32, INT64,
        FLOAT, FLOAT32, FLOAT64, STRING, BOOL,
        DIGITALINPUTPIN, DIGITALOUTPUTPIN,
        ANALOGINPUTPIN, ANALOGOUTPUTPIN,
        DIGITALPIN, ANALOGPIN,
        TIME, VOID, MODULE, ARRAY;
    }

    companion object {
        val Int = Type(EType.INT)
        val Int8 = Type(EType.INT8)
        val Int16 = Type(EType.INT16)
        val Int32 = Type(EType.INT32)
        val Int64 = Type(EType.INT64)
        val Float = Type(EType.FLOAT)
        val Float32 = Type(EType.FLOAT32)
        val Float64 = Type(EType.FLOAT64)
        val String = Type(EType.STRING)
        val Bool = Type(EType.BOOL)
        val Time = Type(EType.TIME)
        val Void = Type(EType.VOID)
        val DigitalPin = Type(EType.DIGITALPIN)
        val AnalogPin = Type(EType.ANALOGPIN)
        val DigitalInputPin = Type(EType.DIGITALINPUTPIN)
        val DigitalOutputPin = Type(EType.DIGITALOUTPUTPIN)
        val AnalogInputPin = Type(EType.ANALOGINPUTPIN)
        val AnalogOutputPin = Type(EType.ANALOGOUTPUTPIN)

        fun createArrayOf(subType: Type): Type {
            return Type(EType.ARRAY, subType)
        }

    }

    override fun equals(other: Any?): Boolean {
        if (other is Type) {
            if (other.main == this.main) {
                if (main == EType.ARRAY) {
                    return this.subType!! == other.subType!!
                } else
                    return true
            } else if(isIntType() && other.isIntType())
                return true
            else if(isFloatType() && other.isFloatType())
                return true
            else if ((main == EType.DIGITALPIN && (other.main == EType.DIGITALINPUTPIN || other.main == EType.DIGITALOUTPUTPIN))
                    || (other.main == EType.DIGITALPIN && (main == EType.DIGITALINPUTPIN || main == EType.DIGITALOUTPUTPIN))
                    || (main == EType.ANALOGPIN && (other.main == EType.ANALOGINPUTPIN || other.main == EType.ANALOGOUTPUTPIN))
                    || (other.main == EType.ANALOGPIN && (main == EType.ANALOGINPUTPIN || main == EType.ANALOGOUTPUTPIN)))
                return true

        }
        return false
    }

    fun exactlyEquals(other: Type): Boolean {
        if (other.main == this.main) {
            if (main == EType.ARRAY) {
                return this.subType!! == other.subType!!
            } else
                return true
        }
        return false
    }

    fun isIntType(): Boolean = main in EType.INT..EType.INT64
    fun isFloatType(): Boolean = main in EType.FLOAT..EType.FLOAT64

    fun isArray(): Boolean = main == EType.ARRAY
    fun getArraySubType(): Type = subType!!

    fun isPin(): Boolean = (main in EType.DIGITALINPUTPIN..EType.ANALOGPIN)

    override fun toString(): String {
        return "$main" + if (isArray()) " <${subType!!}>" else ""
    }

    override fun hashCode(): Int {
        var result = main.hashCode()
        result = 31 * result + (subType?.hashCode() ?: 0)
        return result
    }
}