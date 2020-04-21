package codegeneration

import codegeneration.CodeGenerator.Emitter.emitGlobal
import sablecc.analysis.DepthFirstAdapter
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
        return Emitter.finalize()
    }

    override fun caseABinopExpr(node: ABinopExpr) {


    }
    override fun caseAAdditionBinop(node: AAdditionBinop) {

    }

    override fun caseAIfStmt(node: AIfStmt) {
        emitGlobal("if (")
        node.expr.apply(this)
        emitGlobal(") ")
        node.ifBody.apply(this)
        if (node.elseBody != null) {
            emitGlobal("else")
            node.elseBody.apply(this)
        }
    }

    override fun caseADclStmt(node: ADclStmt) {
        node.type.apply(this)
        emitGlobal(codeStack.pop())

        node.vardcl.first().apply(this)
        emitGlobal( " " + codeStack.pop())
        for (vdcl in node.vardcl.drop(1)) {
            vdcl.apply(this)
            emitGlobal(", " + codeStack.pop() )
        }
        emitGlobal(";")
    }

    override fun outAVardcl(node: AVardcl) {
        if (node.expr != null) {
            val expr = codeStack.pop()
            val identifier = codeStack.pop()

            codeStack.push("$identifier = ${expr!!}")
        }
        // Otherwise just leave the identifier at the top of the stack
    }

    override fun caseAIntValue(node: AIntValue) {
        codeStack.push(node.intliteral.text)
    }

    override fun caseTIdentifier(node: TIdentifier) {
        codeStack.push(node.text)
    }

    override fun caseAIntType(node: AIntType) {
        codeStack.push("int")
    }

    override fun caseATimeType(node: ATimeType) {
        codeStack.push("Time")
    }

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
}