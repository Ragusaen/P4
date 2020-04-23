package semantics.SymbolTable

import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*
import semantics.SymbolTable.Exceptions.CloseScopeZeroException
import semantics.SymbolTable.Exceptions.IdentifierAlreadyDeclaredException
import semantics.SymbolTable.Exceptions.IdentifierUsedBeforeDeclarationException
import semantics.TypeChecking.Type

class SymbolTableBuilder : DepthFirstAdapter() {
    private var currentScope = Scope(null)

    private val namedFunctionTable = mutableMapOf<Pair<String, List<Type>>, Identifier>()
    private val nodeFunctionTable = mutableMapOf<Node, Identifier>()

    private val moduleTable = mutableMapOf<String, ModuleIdentifier>()

    private var rootElementMode = false

    private fun addVar(name:String, identifier: Identifier) {
        // If the name is already used within this scope throw exception
        if (name in currentScope)
            throw IdentifierAlreadyDeclaredException("The $name is already declared.")
        else
            currentScope[name] = identifier
    }

    private fun addFun(node: Node, name:String, params: List<Type>, identifier: Identifier) {
        // If the name is already used within this scope throw exception
        if (Pair(name, params) in namedFunctionTable)
            throw IdentifierAlreadyDeclaredException(" function with $name and $params has already been declared.")
        else {
            namedFunctionTable[Pair(name, params)] = identifier
            nodeFunctionTable[node] = identifier
        }
    }

    private fun addModule(name: String, identifier: ModuleIdentifier) {
        if (name !in moduleTable) {
            moduleTable[name] = identifier
        } else
            throw IdentifierAlreadyDeclaredException("Module with name $name has already been declared.")
    }

    private fun checkHasBeenDeclared(name: String) {
        var tempScope: Scope? = currentScope

        while(tempScope != null) {
            if (tempScope.contains(name))
                return
            tempScope = tempScope.parent
        }

        throw IdentifierUsedBeforeDeclarationException("Variable $name was used before it was declared.")
    }

    private fun openScope() {
        val newScope = Scope(currentScope)
        currentScope.children.add(newScope)
        currentScope = newScope
    }

    private fun closeScope() {
        val parent = currentScope.parent

        if (parent != null)
            currentScope = parent
        else
            throw CloseScopeZeroException("Attempted to close the bottom scope.")
    }

    fun buildSymbolTable(s: Start): SymbolTable {
        caseStart(s)
        if (currentScope.parent != null)
            throw Exception("An unknown error occurred while building symbol table. A scope was not closed as expected.")

        return SymbolTable(namedFunctionTable, nodeFunctionTable, currentScope, moduleTable)
    }

    private fun getTypeFromPType(node:PType): Type {
        return when(node) {
            is AIntType -> Type.Int
            is AFloatType -> Type.Float
            is AStringType -> Type.String
            is ABoolType -> Type.Bool
            is ADigitalinputpinType -> Type.DigitalInputPin
            is ADigitaloutputpinType -> Type.DigitalOututPin
            is AAnaloginputpinType -> Type.AnalogInputPin
            is AAnalogoutputpinType -> Type.AnalogOutputPin
            is ATimeType -> Type.Time
            is AArrayType -> createArrayOfDepth(getTypeFromPType(node.type), (node.sizes as AEmptyIndexlist).filler.size + 1)
            else -> throw Exception("Unsupported node type")
        }
    }

    private fun createArrayOfDepth(type: Type, n: Int): Type {
        var cArr = Type.createArrayOf(type)
        for (i in 1 until n) {
            cArr = Type.createArrayOf(cArr)
        }
        return cArr
    }

    /* Tree traversal */

    /* Tree traversal - Root scope */
    override fun caseAProgram(node: AProgram) {
        // First add all template modules and functions to the symbol table
        rootElementMode = true
        for (re in node.rootElement) {
            // The root element mode is handled in each case, function and template module do no check inner code, dcl is the same.
            // non-template modules should just be skipped
            re.apply(this)
        }

        // Now traverse the rest of the program
        rootElementMode = false
        for (re in node.rootElement) {
            re.apply(this)
        }
    }

    // Only do root element declarations if in root element mode
    override fun caseADclRootElement(node: ADclRootElement?) {
        if (rootElementMode)
            super.caseADclRootElement(node)
    }

    override fun caseAFunctiondcl(node: AFunctiondcl) {
        if (rootElementMode)
            outAFunctiondcl(node)
        else
            super.caseAFunctiondcl(node)
    }

    override fun caseATemplateModuledcl(node: ATemplateModuledcl) {
        if (rootElementMode)
            outATemplateModuledcl(node)
        else
            super.caseATemplateModuledcl(node)
    }

    override fun caseAModuledclRootElement(node: AModuledclRootElement) {
        // Only allow template modules to be considered in root element mode
        if (rootElementMode) {
            if (node.moduledcl is ATemplateModuledcl)
                node.moduledcl.apply(this)
        } else
            super.caseAModuledclRootElement(node)
    }

    /* Tree Traversal - Rest of program */

    override fun inABlockStmt(node: ABlockStmt) = openScope()
    override fun outABlockStmt(node: ABlockStmt) = closeScope()

    override fun inAForStmt(node: AForStmt) = openScope()
    override fun outAForStmt(node: AForStmt) = closeScope()

    override fun inAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            openScope()
        }
    }
    override fun outAInnerModule(node: AInnerModule) {
        if (!node.dcls.isEmpty()) {
            closeScope()
        }
    }

    override fun outAVardcl(node: AVardcl) {
        val name = node.identifier.text
        val ptype = (node.parent() as ADclStmt).type

        addVar(name, Identifier(getTypeFromPType(ptype)))
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val name = node.identifier.text

        checkHasBeenDeclared(name)
    }

    override fun outAFunctionCallExpr(node: AFunctionCallExpr) {
        val name = node.identifier.text
        if (!namedFunctionTable.any {it.key.first == name})
            throw IdentifierUsedBeforeDeclarationException("A function with name $name has not been declared.")
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        openScope()

        // Add each parameter variable to the scope
        node.param.forEach {addVar((it as AParam).identifier.text, Identifier(getTypeFromPType(it.type), true))}
    }
    override fun outAFunctiondcl(node: AFunctiondcl) {
        if (rootElementMode) {
            val name = node.identifier.text!!
            val params = node.param.map { getTypeFromPType((it as AParam).type) }

            val type = if (node.type == null) Type.Void else getTypeFromPType(node.type)

            addFun(node, name, params, Identifier(type))
        } else {
            closeScope()
        }
    }

    override fun inATemplateModuledcl(node: ATemplateModuledcl) {
        openScope()

        // Add each parameter variable to the scope
        node.param.forEach {addVar((it as AParam).identifier.text, Identifier(getTypeFromPType(it.type)))}
    }
    override fun outATemplateModuledcl(node: ATemplateModuledcl) {
        if (rootElementMode) {
            val name = node.identifier.text
            val params = node.param.map { getTypeFromPType((it as AParam).type) }

            addModule(name, ModuleIdentifier(params))
        }
        else
            closeScope()
    }

    override fun outAModuledclStmt(node: AModuledclStmt) {
        val name = node.instance.text

        addVar(name, Identifier(Type.Module))
    }

    override fun outANamedModuledcl(node: ANamedModuledcl) {
        val name = node.identifier.text

        addVar(name, Identifier(Type.Module))
    }
}
