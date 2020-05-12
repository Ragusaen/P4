package semantics.typeChecking

class Type private constructor(private val main: EType, private val subType: Type? = null) {
    private enum class EType {
        INT, FLOAT, STRING, BOOL,
        DIGITALINPUTPIN, DIGITALOUTPUTPIN,
        ANALOGINPUTPIN, ANALOGOUTPUTPIN,
        DIGITALPIN, ANALOGPIN,
        TIME, VOID, MODULE, ARRAY;
    }

    companion object {
        val Int = Type(EType.INT)
        val Float = Type(EType.FLOAT)
        val String = Type(EType.STRING)
        val Bool = Type(EType.BOOL)
        val Time = Type(EType.TIME)
        val Void = Type(EType.VOID)
        val Module = Type(EType.MODULE)
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
            }
        }
        return false
    }

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