package typeChecking

class TypeRule(val lSide : Type, val operator : OperatorType.Companion.Operator, val rSide : Type, val returnType : Type) {}