package semantics.symbolTable

import ErrorHandler
import ErrorTraverser
import sablecc.node.*
import semantics.symbolTable.errors.CloseScopeZeroException
import semantics.symbolTable.errors.IdentifierAlreadyDeclaredError
import semantics.symbolTable.errors.IdentifierUsedBeforeDeclarationError
import semantics.typeChecking.Type
import semantics.typeChecking.errors.IdentifierNotDeclaredError

class SymbolTableBuilder(errorHandler: ErrorHandler) : ErrorTraverser(errorHandler) {
    private var currentScope = Scope(null)
    private var currentVarPrefix = ""

    private val namedFunctionTable = mutableMapOf<Pair<String, List<Type>>, Identifier>()
    private val templateModuleTable = mutableMapOf<String, TemplateModuleIdentifier>()
    private val moduleTable = mutableMapOf<String, String>()
    private val nodeModuleTable = mutableMapOf<Node, String>()

    private var rootElementMode = false

    private var anonModuleCount = 0
    private fun nextAnonName(): String {
        return "AnonymousModule${anonModuleCount++}"
    }

    private fun addVar(name:String, type: Type, isInit: Boolean = false) {
        // If the name is already used within this scope throw exception
        if (name in currentScope.variables)
            error(IdentifierAlreadyDeclaredError("The variable $name has already been declared."))
        else
            currentScope.variables[name] = Identifier(type, currentVarPrefix + name, isInit)
    }

    private fun addFun(name:String, params: List<Type>, identifier: Identifier) {
        // If the name is already used within this scope throw exception
        if (Pair(name, params) in namedFunctionTable)
            error(IdentifierAlreadyDeclaredError("Function with the name $name with parameters: $params has already been declared."))
        else {
            namedFunctionTable[Pair(name, params)] = identifier
        }
    }

    private fun addTemplateModule(node: Node, name: String, identifier: TemplateModuleIdentifier) {
        if (name !in templateModuleTable) {
            templateModuleTable[name] = identifier
            nodeModuleTable[node] = name
        } else
            throw IdentifierAlreadyDeclaredError("Template module with the name $name has already been declared.")
    }

    private fun addModule(node: Node, name: String, templateOf: String = name) {
        if (!moduleTable.containsKey(name)) {
            if (name != templateOf && !templateModuleTable.containsKey(templateOf)) {
                error(IdentifierNotDeclaredError("No template module with name $templateOf exists."))
            }

            moduleTable[name] = templateOf
            nodeModuleTable[node] = name
        } else
            throw IdentifierAlreadyDeclaredError("Module with the name $name has already been declared")
    }

    private fun checkHasBeenDeclared(name: String) {
        currentScope.findVar(name) ?:
			this.error(IdentifierUsedBeforeDeclarationError("The variable $name was used before it was declared."))
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
            throw Exception("An unknown error occurred while building the symbol table. A scope was not closed as expected.")

        return SymbolTable(namedFunctionTable, currentScope, templateModuleTable, moduleTable, nodeModuleTable).reset()
    }

    /* Tree traversal */

    /* Tree traversal - Root scope */
    override fun caseAProgram(node: AProgram) {
        // First add all template modules and functions to the symbol table
        rootElementMode = true
        // The root element mode is handled in each case, function and template module do no check inner code, dcl is the same
        // Do modules before others to ensure instances of template modules can be created in root
        val (modules, other) = node.rootElement.partition{ it is AModuledclRootElement}
        modules.forEach { it.apply(this) }
        other.forEach { it.apply(this) }

        // Now traverse the rest of the program
        rootElementMode = false
        for (re in node.rootElement) {
            re.apply(this)
        }
    }

    // Only do root element declarations if in root element mode
    override fun caseADclRootElement(node: ADclRootElement) {
        if (rootElementMode) {
            currentVarPrefix = "global_"
            super.caseADclRootElement(node)
        }
    }

    override fun caseAFunctiondcl(node: AFunctiondcl) {
        if (rootElementMode) {
            val name = node.identifier.text!!
            val params = Helper.getFunParams(node)

            val type = if (node.type == null) Type.Void else Helper.getTypeFromPType(node.type)

            addFun(name, params, Identifier(type, name))
        }
        else
            super.caseAFunctiondcl(node)
    }

    override fun caseATemplateModuledcl(node: ATemplateModuledcl) {
        if (rootElementMode) {
            val name = node.identifier.text
            val params = node.param.map { Helper.getTypeFromPType((it as AParam).type) }

            addTemplateModule(node, name, TemplateModuleIdentifier(params))
        }
        else
            super.caseATemplateModuledcl(node)
    }

    override fun caseAInstanceModuledcl(node: AInstanceModuledcl) {
        if (rootElementMode) {
            val name = node.identifier?.text ?: nextAnonName()
            addModule(node, name)
        }
        else
            super.caseAInstanceModuledcl(node)
    }

    /* Tree Traversal - Rest of program */
    override fun inABlockStmt(node: ABlockStmt) = openScope()
    override fun outABlockStmt(node: ABlockStmt) = closeScope()

    override fun inAForStmt(node: AForStmt) {
        openScope()
        addVar(node.identifier.text, Type.Int, true)
    }
    override fun outAForStmt(node: AForStmt) = closeScope()

    override fun outAVardcl(node: AVardcl) {
        val name = node.identifier.text
        val ptype = (node.parent() as ADclStmt).type

        addVar(name, Helper.getTypeFromPType(ptype))
    }

    override fun outAIdentifierValue(node: AIdentifierValue) {
        val name = node.identifier.text

        checkHasBeenDeclared(name)
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        val name = node.identifier.text

        checkHasBeenDeclared(name)
    }

    override fun outAFunctionCallExpr(node: AFunctionCallExpr) {
        val name = node.identifier.text
        if (!namedFunctionTable.any {it.key.first == name})
            error(IdentifierUsedBeforeDeclarationError("A function with name $name has not been declared."))
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        openScope()

        // Add each parameter variable to the scope
        node.param.forEach {errorHandler.setLineAndPos((it as AParam).identifier); addVar((it as AParam).identifier.text, Helper.getTypeFromPType(it.type), true)}
    }
    override fun outAFunctiondcl(node: AFunctiondcl) {
        closeScope()
    }

    override fun inATemplateModuledcl(node: ATemplateModuledcl) {
        openScope()

        val name = node.identifier.text
        currentVarPrefix = "$name->"

        // Add each parameter variable to the scope
        node.param.forEach {errorHandler.setLineAndPos((it as AParam).identifier); addVar((it as AParam).identifier.text, Helper.getTypeFromPType(it.type), true)}
    }

    override fun outATemplateModuledcl(node: ATemplateModuledcl) {
        closeScope()
    }

    override fun outAModuledclStmt(node: AModuledclStmt) {
        val name = node.instance.text
        val template = node.template.text

        addModule(node, name, template)
    }

    override fun inAInstanceModuledcl(node: AInstanceModuledcl) {
        openScope()
        val name = nodeModuleTable[node]!!
        currentVarPrefix = name + "_"
    }

    override fun outAInstanceModuledcl(node: AInstanceModuledcl) {
        closeScope()
    }

    override fun caseAInnerModule(node: AInnerModule) {
        inAInnerModule(node)
        node.dcls.forEach {it.apply(this)}
        currentVarPrefix = ""
        node.moduleStructure.apply(this)
        outAInnerModule(node)
    }
}
