package semantics.typeChecking

import sablecc.node.*

class  OperatorType {
    companion object{
        //Semantic type rules
        private val typeRules = listOf(
                TypeRule(Type.Int, Operator.ADDITION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.ADDITION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.ADDITION, Type.Time, Type.Time),
                TypeRule(Type.String, Operator.ADDITION, Type.String, Type.String),
                TypeRule(Type.Int, Operator.SUBTRACTION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.SUBTRACTION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.SUBTRACTION, Type.Time, Type.Time),
                TypeRule(Type.Int, Operator.MULTIPLICATION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.MULTIPLICATION, Type.Float, Type.Float),
                TypeRule(Type.Int, Operator.MULTIPLICATION, Type.Time, Type.Time),
                TypeRule(Type.Time, Operator.MULTIPLICATION, Type.Int, Type.Time),
                TypeRule(Type.Int, Operator.DIVISION, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.DIVISION, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.DIVISION, Type.Time, Type.Int),
                TypeRule(Type.Int, Operator.MODULO, Type.Int, Type.Int),
                TypeRule(Type.Float, Operator.MODULO, Type.Float, Type.Float),
                TypeRule(Type.Time, Operator.MODULO, Type.Time, Type.Int),
                TypeRule(Type.Int, Operator.LESSTHAN, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.LESSTHAN, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.LESSTHAN, Type.Time, Type.Bool),
                TypeRule(Type.Int, Operator.GREATERTHAN, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.GREATERTHAN, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.GREATERTHAN, Type.Time, Type.Bool),
                TypeRule(Type.Int, Operator.LESSTHANOREQUAL, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.LESSTHANOREQUAL, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.LESSTHANOREQUAL, Type.Time, Type.Bool),
                TypeRule(Type.Int, Operator.GREATERTHANOREQUAL, Type.Int, Type.Bool),
                TypeRule(Type.Float, Operator.GREATERTHANOREQUAL, Type.Float, Type.Bool),
                TypeRule(Type.Time, Operator.GREATERTHANOREQUAL, Type.Time, Type.Bool),
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
            LESSTHANOREQUAL,
            GREATERTHANOREQUAL,
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
                ALessthanorequalBinop::class.java to Operator.LESSTHANOREQUAL,
                AGreaterthanorequalBinop::class.java to Operator.GREATERTHANOREQUAL,
                AEqualBinop::class.java to Operator.EQUALS,
                AAndBinop::class.java to Operator.AND,
                AOrBinop::class.java to Operator.OR
        )

        fun getReturnType(lSide : Type, op : PBinop, rSide: Type) : Type?{
            return typeRules.firstOrNull { it.lSide == lSide && it.operator == opMap[op::class.java] && it.rSide == rSide }?.returnType
        }

        private val opToStringMap = mapOf(
                AAdditionBinop::class.java to "+",
                ASubtractionBinop::class.java to "-",
                ADivisionBinop::class.java to "/",
                AMultiplicationBinop::class.java to "*",
                AModuloBinop::class.java to "%",
                ALessthanBinop::class.java to "<",
                AGreaterthanBinop::class.java to ">",
                AEqualBinop::class.java to "==",
                AAndBinop::class.java to "and",
                AOrBinop::class.java to "or"
        )

        fun opNodeToString(node:PBinop) = opToStringMap[node::class.java]
    }
}