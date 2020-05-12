package semantics.typeChecking

class TypeRule(val lside : Type, val operator : OperatorType.Companion.Operator, val rside : Type, val returnType : Type) {}