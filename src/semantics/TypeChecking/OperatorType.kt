package semantics.TypeChecking

import sablecc.node.*

class  OperatorType {
    companion object{
        //Semantic type rules
        val typeRules = listOf<TypeRule>(
                TypeRule(Type.Int, Operator.ADDITION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.ADDITION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.ADDITION, Type.Time, Type.Time),
                TypeRule(Type.String, Operator.ADDITION, Type.String, Type.String),
                TypeRule(Type.Int, Operator.SUBTRACTION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.SUBTRACTION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.SUBTRACTION, Type.Time, Type.Time),
                TypeRule(Type.Int, Operator.MULTIPLICATION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.MULTIPLICATION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.MULTIPLICATION, Type.Time, Type.Time),
                TypeRule(Type.Time, Operator.MULTIPLICATION, Type.Int, Type.Time),
                TypeRule(Type.Int, Operator.DIVISION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.DIVISION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.DIVISION, Type.Time, Type.Int),
                TypeRule(Type.Time, Operator.DIVISION, Type.Int, Type.Time),
                TypeRule(Type.Int, Operator.MODULO, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.MODULO, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.MODULO, Type.Time, Type.Int),
                TypeRule(Type.Int, Operator.LESSTHAN, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.LESSTHAN, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.LESSTHAN, Type.Time, Type.Bool),
                TypeRule(Type.Int, Operator.GREATERTHAN, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.GREATERTHAN, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.GREATERTHAN, Type.Time, Type.Bool),
                TypeRule(Type.Int, Operator.EQUALS, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.EQUALS, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.EQUALS, Type.Time, Type.Bool),
                TypeRule(Type.Bool, Operator.EQUALS, Type.Bool, Type.Bool),
                TypeRule(Type.String, Operator.EQUALS, Type.String, Type.Bool),
                TypeRule(Type.Bool, Operator.AND, Type.Bool, Type.Bool),
                TypeRule(Type.Bool, Operator.OR, Type.Bool, Type.Bool)
                )

        enum class Operator{
            ADDITION,
            SUBTRACTION,
            DIVISION,
            MULTIPLICATION,
            MODULO,
            LESSTHAN,
            GREATERTHAN,
            EQUALS,
            AND,
            OR
        }

        private val opMap = mapOf(
                AAdditionBinop::class.java to Operator.ADDITION,
                ASubtractionBinop::class.java to Operator.SUBTRACTION,
                ADivisionBinop::class.java to Operator.DIVISION,
                AMultiplicationBinop::class.java to Operator.MULTIPLICATION,
                AModuloBinop::class.java to Operator.MODULO,
                ALessthanBinop::class.java to Operator.LESSTHAN,
                AGreaterthanBinop::class.java to Operator.GREATERTHAN,
                AEqualBinop::class.java to Operator.EQUALS,
                AAndBinop::class.java to Operator.AND,
                AOrBinop::class.java to Operator.OR
        )

        fun getReturnType(lside : Type, op : PBinop, rside: Type) : Type?{
            return typeRules.firstOrNull { it.lside == lside && it.operator == opMap[op::class.java] && it.rside == rside }?.returnType
        }
    }
}