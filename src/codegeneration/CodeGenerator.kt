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

    override fun caseAUnopExpr(node: AUnopExpr) {
        node.expr.apply(this)
        val expr = codeStack.pop()


    }

    override fun caseABinopExpr(node: ABinopExpr) {
        var s:String = ""

        node.l.apply(this)
        node.r.apply(this)
        node.binop.apply(this)
        val operator = codeStack.pop()
        val r = codeStack.pop()
        val l = codeStack.pop()

        if(typeTable[node] == Type.STRING) {
            TODO()
        }
        else {
            s = l + operator + r
        }

        codeStack.push(s)
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

    override fun caseARelationBinop(node: ARelationBinop) {
        TODO() //Unknown binary operator (?)
    }

    override fun caseASubtractionBinop(node: ASubtractionBinop) {
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
            codeStack.push("if ($cond) $ifBody else $elseBody")
        } else {
            codeStack.push("if ($cond) $ifBody")
        }
    }

    override fun caseAWhileStmt(node: AWhileStmt) {
        node.body.apply(this)
        node.condition.apply(this)
        val cond = codeStack.pop()
        val body = codeStack.pop()
        codeStack.push("while ($cond) $body")
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

        codeStack.push("for ($init; $cond; $update) $body")
    }

    override fun caseADclStmt(node: ADclStmt) {
        node.type.apply(this)
        val type = codeStack.pop()

        node.vardcl.first().apply(this)
        var vardcls = codeStack.pop()
        for (v in node.vardcl.drop(1)) {
            v.apply(this)
            vardcls += ", " + codeStack.pop()
        }
        codeStack.push("$type $vardcls")
    }

    override fun caseAAssignStmt(node: AAssignStmt) {
        node.expr.apply(this)
        node.identifier.apply(this)
        val id = codeStack.pop()
        val expr = codeStack.pop()

        if (node.binop != null) {
            node.binop.apply(this)
            val binop = codeStack.pop()
            codeStack.push("$id $binop= $expr;")
        } else {
            codeStack.push("$id = $expr;")
        }
    }

    override fun caseAVardcl(node: AVardcl) {
        if (node.expr != null) {
            node.expr.apply(this)
            val expr = codeStack.pop()
            node.identifier.apply(this)
            val identifier = codeStack.pop()

            codeStack.push("$identifier = $expr")
        }
        // Otherwise just leave the identifier at the top of the stack
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
        codeStack.push(";")
    }

    override fun caseABreakStmt(node: ABreakStmt?) {
        codeStack.push("break;")
    }

    override fun caseAContinueStmt(node: AContinueStmt?) {
        codeStack.push("continue;")
    }

    override fun caseAExprStmt(node: AExprStmt) {
        node.expr.apply(this)
        val expr = codeStack.pop()
        codeStack.push(expr)
    }

    override fun caseAReturnStmt(node: AReturnStmt) {
        if (node.expr != null) {
            node.expr.apply(this)
            val expr = codeStack.pop()
            codeStack.push("return $expr;")
        } else {
            codeStack.push("return;")
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
        codeStack.push("char *")
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
}