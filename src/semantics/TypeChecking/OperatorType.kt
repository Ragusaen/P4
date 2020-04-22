package semantics.TypeChecking

import sablecc.node.*

class  OperatorType {
    companion object {
        private val operandTypes = mapOf(
            setOf(AAdditionBinop::class.java) to setOf(Type.FLOAT, Type.INT, Type.STRING, Type.TIME),

            setOf(AGreaterthanBinop::class.java, ALessthanBinop::class.java, AMultiplicationBinop::class.java,
                    ASubtractionBinop::class.java, AModuloBinop::class.java,
                    APlusUnop::class.java, AMinusUnop::class.java) to setOf(Type.INT, Type.FLOAT, Type.TIME),

            setOf(ADivisionBinop::class.java) to setOf(Type.INT, Type.FLOAT),

            setOf(AAndBinop::class.java, AOrBinop::class.java, ANotUnop::class.java) to setOf(Type.BOOL),

            setOf(AEqualBinop::class.java) to setOf(Type.BOOL, Type.INT, Type.FLOAT, Type.TIME, Type.STRING)
        ).map {o -> Pair(o.key.map{ it.simpleName!! }.toSet(), o.value)}

        private val returnTypes = mapOf(
                setOf(AAndBinop::class.java, AOrBinop::class.java, ANotUnop::class.java, AGreaterthanBinop::class.java,
                      ALessthanBinop::class.java, AEqualBinop::class.java) to setOf(Type.BOOL),

                setOf(AAdditionBinop::class.java) to setOf(Type.FLOAT, Type.INT, Type.STRING, Type.TIME),

                setOf(AMultiplicationBinop::class.java, ASubtractionBinop::class.java,
                      AModuloBinop::class.java, APlusUnop::class.java, AMinusUnop::class.java)
                      to setOf(Type.INT, Type.FLOAT, Type.TIME),

                setOf(ADivisionBinop::class.java) to setOf(Type.INT, Type.FLOAT)
        ).map {o -> Pair(o.key.map{ it.simpleName!! }.toSet(), o.value)}


        // returns the types that are compatible with the operator
        fun getOperandTypes(className: String): Set<Type> {
            return operandTypes.first{ className in it.first}.second
        }

        // returns the types that can be returned from the operator
        fun getReturnTypes(className: String): Set<Type> {
            return returnTypes.first{className in it.first}.second
        }
    }
}