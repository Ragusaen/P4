package codegeneration

import sablecc.node.*
import semantics.SymbolTable.ScopedTraverser
import semantics.SymbolTable.SymbolTable
import semantics.TypeChecking.Type
import java.util.*

class CodeGenerator(private val typeTable: MutableMap<Node, Type>, symbolTable: SymbolTable) : ScopedTraverser(symbolTable) {
    private object Emitter {
        private var setupCode = ""
        private var globalCode = ""
        private var loopCode = ""

        fun emitSetup(code: String) {
            setupCode += code
        }
        fun emitGlobal(code: String) {
            globalCode += code
        }
        fun emitLoop(code: String) {
            loopCode += code
        }

        fun finalize(): String ="$globalCode \nvoid setup() { $setupCode } \nvoid loop() { $loopCode }"
    }

    var codeStack = Stack<String>()

    fun generate(startNode: Start): String {
        caseStart(startNode)
        for (i in codeStack)
            Emitter.emitGlobal(i)
        return Emitter.finalize()
    }

    private fun getCode(node: Node): String {
        node.apply(this)
        return codeStack.pop()
    }

    override fun caseAUnopExpr(node: AUnopExpr) {
        node.expr.apply(this)
        val expr = codeStack.pop()

        node.expr.apply(this)
        val unop = codeStack.pop()

        codeStack.push("${unop}${expr}")
    }

    override fun caseAMinusUnop(node: AMinusUnop) {
        codeStack.push("-")
    }

    override fun caseAPlusUnop(node: APlusUnop?) {
        codeStack.push("+")
    }

    override fun caseANotUnop(node: ANotUnop) {
        codeStack.push("!")
    }

    override fun caseABinopExpr(node: ABinopExpr) {
        val operator = getCode(node.binop)
        val r = getCode(node.r)
        val l = getCode(node.l)

        val b = operator == "=="
        val c = typeTable[node.l] == Type.STRING
        val d  = typeTable[node.r] == Type.STRING
        // Special case for strings where function calls must be made
        if(typeTable[node] == Type.STRING) {
            if (operator == "+")
                codeStack.push("concatstr($l, $r)")
            else
                throw java.lang.Exception()
        }
        else if(operator == "==" && typeTable[node.l] == Type.STRING && typeTable[node.r] == Type.STRING) {
            codeStack.push("equalstr($l, $r)")
        }
        else {
            codeStack.push(l + operator + r)
        }
    }

    override fun caseAAdditionBinop(node: AAdditionBinop) {
        codeStack.push("+")
    }

    override fun caseADivisionBinop(node: ADivisionBinop) {
        codeStack.push("/")
    }

    override fun caseAAndBinop(node: AAndBinop) {
        codeStack.push("&&")
    }

    override fun caseAEqualBinop(node: AEqualBinop) {
        codeStack.push("==")
    }

    override fun caseAGreaterthanBinop(node: AGreaterthanBinop) {
        codeStack.push(">")
    }

    override fun caseALessthanBinop(node: ALessthanBinop) {
        codeStack.push("<")
    }

    override fun caseAModuloBinop(node: AModuloBinop) {
        codeStack.push("%")
    }

    override fun caseAMultiplicationBinop(node: AMultiplicationBinop) {
        codeStack.push("*")
    }

    override fun caseAOrBinop(node: AOrBinop) {
        codeStack.push("||")
    }

    override fun caseASubtractionBinop(node: ASubtractionBinop?) {
        codeStack.push("-")
    }

    override fun caseAIfStmt(node: AIfStmt) {
        node.ifBody.apply(this)
        node.expr.apply(this)
        val cond = codeStack.pop()
        val ifBody = codeStack.pop()
        if (node.elseBody != null) {
            node.elseBody.apply(this)
            val elseBody = codeStack.pop()
            codeStack.push("if ($cond) $ifBody else $elseBody\n")
        } else {
            codeStack.push("if ($cond) $ifBody\n")
        }
    }

    override fun caseAWhileStmt(node: AWhileStmt) {
        node.body.apply(this)
        node.condition.apply(this)
        val cond = codeStack.pop()
        val body = codeStack.pop()
        codeStack.push("while ($cond) $body\n")
    }

    override fun caseAForStmt(node: AForStmt) {
        node.body.apply(this)
        node.update.apply(this)
        node.condition.apply(this)
        node.init.apply(this)
        val init = codeStack.pop()
        val cond = codeStack.pop()
        val update = codeStack.pop()
        val body = codeStack.pop()

        codeStack.push("for ($init; $cond; $update) $body\n")
    }

    override fun caseADclStmt(node: ADclStmt) {
        node.type.apply(this)
        val type = codeStack.pop()

        node.vardcl.first().apply(this)
        var vardcls = codeStack.pop()
        for (v in node.vardcl.drop(1)) {
            vardcls += ", " + getCode(v)
        }
        codeStack.push("$type $vardcls;\n")
    }

    override fun caseAAssignStmt(node: AAssignStmt) {
        node.expr.apply(this)
        node.identifier.apply(this)
        val id = codeStack.pop()
        val expr = codeStack.pop()

        if (node.binop != null) {
            node.binop.apply(this)
            val binop = codeStack.pop()
            codeStack.push("$id $binop= $expr;\n")
        } else {
            codeStack.push("$id = $expr;\n")
        }
    }

    override fun caseAVardcl(node: AVardcl) {
        val identifier = getCode(node.identifier)

        if (node.expr != null) {
            val expr = getCode(node.expr)
            codeStack.push("$identifier = $expr")
        } else {
            codeStack.push(identifier)
        }
    }
	
    override fun caseABlockStmt(node: ABlockStmt) {
        var block = ""
        for (s in node.stmt) {
            s.apply(this)
            block += codeStack.pop() + "\n"
        }
        codeStack.push("{\n $block \n}")
    }

    override fun caseANoStmtStmt(node: ANoStmtStmt?) {
        codeStack.push(";\n")
    }

    override fun caseABreakStmt(node: ABreakStmt?) {
        codeStack.push("break;\n")
    }

    override fun caseAContinueStmt(node: AContinueStmt?) {
        codeStack.push("continue;\n")
    }

    override fun caseAExprStmt(node: AExprStmt) {
        node.expr.apply(this)
        val expr = codeStack.pop()
        codeStack.push("$expr;\n")
    }

    override fun caseAReturnStmt(node: AReturnStmt) {
        if (node.expr != null) {
            node.expr.apply(this)
            val expr = codeStack.pop()
            codeStack.push("return $expr;\n")
        } else {
            codeStack.push("return;\n")
        }
    }


    // Immediate handling
    override fun caseTTimeliteral(node: TTimeliteral) {
        val li = node.text.indexOfFirst { it.isLetter() }
        val num = node.text.substring(0, li).toFloat()
        val suffix = node.text.substring(li)

        val value = when(suffix) {
            "h" -> num * 3600000
            "m" -> num * 60000
            "s" -> num * 1000
            "ms" -> num
            else -> throw Exception("Unexpected suffix $suffix of time literal: ${node.text}")
        }

        codeStack.push(value.toInt().toString())
    }

    override fun caseTStringliteral(node: TStringliteral) {
        codeStack.push(node.text)
    }

    // Value handling
    override fun caseTIdentifier(node: TIdentifier) {
        codeStack.push(node.text)
    }

    override fun caseAIntValue(node: AIntValue) {
        codeStack.push(node.intliteral.text)
    }

    override fun caseAFloatValue(node: AFloatValue) {
        codeStack.push(node.floatliteral.text)
    }

    override fun caseABoolValue(node: ABoolValue) {
        codeStack.push(node.boolliteral.text)
    }

    override fun caseACharValue(node: ACharValue) {
        codeStack.push(node.charliteral.text)
    }

    override fun caseATimeValue(node: ATimeValue) {
        node.timeliteral.apply(this)
    }

    // Type handling
    override fun caseAIntType(node: AIntType) {
        codeStack.push("int")
    }

    override fun caseAFloatType(node: AFloatType) {
        codeStack.push("float")
    }

    override fun caseATimeType(node: ATimeType) {
        codeStack.push("Time")
    }

    override fun caseABoolType(node: ABoolType) {
        codeStack.push("Bool")
    }

    override fun caseACharType(node: ACharType) {
        codeStack.push("char")
    }

    override fun caseAStringType(node: AStringType) {
        codeStack.push("char*")
    }

    override fun caseAIdentifierValue(node: AIdentifierValue) {
        codeStack.push(node.identifier.text)
    }

    override fun caseAFunctiondcl(node: AFunctiondcl) {
        val identifier = getCode(node.identifier)
        val type = if (node.type == null) "void" else getCode(node.type)

        var param = ""
        if (node.param != null) {
            param += getCode(node.param.first())
            for (p in node.param.drop(1)) {
                param += ", " + getCode(p)
            }
        }

        val body = getCode(node.body)
        codeStack.push("$type $identifier ($param)\n$body")
    }

    override fun caseAParam(node: AParam) {
        val identifier = getCode(node.identifier)
        val type = getCode(node.type)

        codeStack.push("$type $identifier")
    }

    override fun caseADigitalinputpinType(node: ADigitalinputpinType?) {
        codeStack.push("DigitalInputPin")
    }

    override fun caseADigitaloutputpinType(node: ADigitaloutputpinType?) {
        codeStack.push("DigitalOutputPin")
    }

    override fun caseAAnaloginputpinType(node: AAnaloginputpinType?) {
        codeStack.push("AnalogOutputPin")
    }

    override fun caseAAnalogoutputpinType(node: AAnalogoutputpinType?) {
        codeStack.push("AnalogOutputPin")
    }

    override fun caseAFunctionCallExpr(node: AFunctionCallExpr) {
        val identifier = getCode(node.identifier)

        var arguments = ""
        if (node.expr != null) {
            arguments += getCode(node.expr.first())
            for (arg in node.expr.drop(1)) {
                arguments += ", " + getCode(arg)
            }
        }

        codeStack.push("$identifier($arguments)")
    }
}