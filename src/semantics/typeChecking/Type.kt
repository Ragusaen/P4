package semantics.typeChecking

class Type private constructor(private val main: EType, private val subType: Type? = null) {
    private enum class EType {
        INT, INT8, INT16, INT32, INT64,
        UINT, UINT8, UINT16, UINT32, UINT64,
        FLOAT, STRING, BOOL,
        DIGITALINPUTPIN, DIGITALOUTPUTPIN,
        ANALOGINPUTPIN, ANALOGOUTPUTPIN,
        DIGITALPIN, ANALOGPIN,
        TIME, VOID, ARRAY;
    }

    companion object {
        val Int = Type(EType.INT)
        val Int8 = Type(EType.INT8)
        val Int16 = Type(EType.INT16)
        val Int32 = Type(EType.INT32)
        val Int64 = Type(EType.INT64)
        val Uint = Type(EType.UINT)
        val Uint8 = Type(EType.UINT8)
        val Uint16 = Type(EType.UINT16)
        val Uint32 = Type(EType.UINT32)
        val Uint64 = Type(EType.UINT64)
        val Float = Type(EType.FLOAT)
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

    fun isIntType(): Boolean = main in EType.INT..EType.UINT64
    fun isArray(): Boolean = main == EType.ARRAY

    fun getArraySubType(): Type = subType!!

    override fun toString(): String {
        return "$main" + if (isArray()) " <${subType!!}>" else ""
    }

    override fun hashCode(): Int {
        var result = main.hashCode()
        result = 31 * result + (subType?.hashCode() ?: 0)
        return result
    }
}