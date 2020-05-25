package semantics.symbolTable

import ErrorHandler
import ErrorTraverser
import getOtherPointFromToken
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
    private val templateInstances = mutableMapOf<String, Int>()

    private var rootElementMode = false

    private var anonModuleCount = 0
    private fun nextAnonName(): String {
        return "AnonymousModule${anonModuleCount++}"
    }

    private fun addVar(identifierToken: TIdentifier, type: Type, isInit: Boolean = false) {
        errorHandler.setLineAndPos(identifierToken)
        // If the name is already used within this scope throw exception
        val name = identifierToken.text
        if (name in currentScope.variables) {
            val otherToken = currentScope.findVar(name)!!.token
            error(IdentifierAlreadyDeclaredError("The variable $name has already been declared.",
                getOtherPointFromToken(otherToken, "Previous declaration was here.")))
        }
        else
            currentScope.variables[name] = Identifier(type, currentVarPrefix + name, identifierToken, isInit)
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
                error(IdentifierNotDeclaredError("No template module with name $templateOf exists. Candidates are ${templateModuleTable.map { it.key }.joinToString(", ")}"))
            }

            moduleTable[name] = templateOf
            nodeModuleTable[node] = name
        } else {
            val otherNode = nodeModuleTable.filterValues { it == name }.keys.first()

            val token: Token? = when (otherNode) {
                is AModuledclStmt -> otherNode.instance
                is AInstanceModuledcl -> otherNode.identifier
                else -> null
            }

            val otherPoint = if (token != null) getOtherPointFromToken(token, "Previous declaration was here.") else null

            errorHandler.compileError(IdentifierAlreadyDeclaredError("Module with the name $name has already been declared", otherPoint))
        }
    }

    private fun checkHasBeenDeclared(identifier: TIdentifier) {
        val name = identifier.text
        val match = currentScope.findVar(name)
        if (match == null) {
            val nearestMatch = currentScope.nearestMatch(name)

            errorHandler.setLineAndPos(identifier)
            if (nearestMatch != null && nearestMatch.second > name.length / 2)
                error(IdentifierUsedBeforeDeclarationError("The variable $name does not exist. Closest match was ${nearestMatch.first}"))
            else
                error(IdentifierUsedBeforeDeclarationError("The variable $name does not exist."))
        }
    }

    private fun openScope() {
        val newScope = Scope(parent=currentScope)
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

        return SymbolTable(namedFunctionTable, currentScope, templateModuleTable, moduleTable, nodeModuleTable, templateInstances).reset()
    }

    /* Tree traversal */

    /* Tree traversal - Root scope */
    override fun caseAProgram(node: AProgram) {
        // First add all template modules and functions to the symbol table
        rootElementMode = true
        // The root element mode is handled in each case, function and template module do no check inner code, vardcl is the same
        // Do modules then functions then rest to ensure instances of template modules can be created in root and functions can be used to initialize variables
        val (modules, other) = node.rootElement.partition{ it is AModuledclRootElement}
        val (functions, rest) = other.partition {it is AFunctiondclRootElement}
        modules.forEach { it.apply(this) }
        functions.forEach { it.apply(this) }
        rest.forEach { it.apply(this) }

        // Now traverse the rest of the program
        rootElementMode = false
        for (re in node.rootElement) {
            re.apply(this)
        }
    }

    // Only do root element declarations if in root element mode
    override fun caseADclRootElement(node: ADclRootElement) {
        currentVarPrefix = "global_"
        if (rootElementMode)
            super.caseADclRootElement(node)
    }

    override fun outAVardcl(node: AVardcl) {
        val ptype = (node.parent() as ADclStmt).type

        addVar(node.identifier, Helper.getTypeFromPType(ptype))
    }

    override fun caseAModuledclStmt(node: AModuledclStmt) {
        if (rootElementMode) {
            val name = node.instance.text
            val template = node.template.text

            errorHandler.setLineAndPos(node.instance)
            if (name == template) {
                error(IdentifierAlreadyDeclaredError("Cannot create instance with same name as template."))
            }

            errorHandler.setLineAndPos(node.template)
            templateInstances[name] = templateInstances.size
            addModule(node, name, template)
        } else
            super.caseAModuledclStmt(node)

    }

    override fun caseAFunctiondcl(node: AFunctiondcl) {
        if (rootElementMode) {
            val name = node.identifier.text!!
            val params = Helper.getFunParams(node)

            val type = if (node.type == null) Type.Void else Helper.getTypeFromPType(node.type)

            addFun(name, params, Identifier(type, name, node.identifier))
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

            if (node.identifier != null)
                errorHandler.setLineAndPos(node.identifier)
            addModule(node, name)
        }
        else
            super.caseAInstanceModuledcl(node)
    }

    override fun caseAInitRootElement(node: AInitRootElement) {
        currentVarPrefix = ""
        if (!rootElementMode)
            super.caseAInitRootElement(node)
    }

    /* Tree Traversal - Rest of program */
    override fun inABlockStmt(node: ABlockStmt) = openScope()
    override fun outABlockStmt(node: ABlockStmt) = closeScope()

    override fun inAForStmt(node: AForStmt) {
        openScope()
        addVar(node.identifier, Type.Int, true)
    }
    override fun outAForStmt(node: AForStmt) = closeScope()

    override fun outAIdentifierValue(node: AIdentifierValue) {
        checkHasBeenDeclared(node.identifier)
    }

    override fun outAAssignStmt(node: AAssignStmt) {
        checkHasBeenDeclared(node.identifier)
    }

    override fun outAFunctionCallExpr(node: AFunctionCallExpr) {
        val name = node.identifier.text
        if (!namedFunctionTable.any {it.key.first == name})
            error(IdentifierUsedBeforeDeclarationError("A function with name $name has not been declared."))
    }

    override fun inAFunctiondcl(node: AFunctiondcl) {
        openScope()
        currentVarPrefix = ""
        // Add each parameter variable to the scope
        node.param.forEach {errorHandler.setLineAndPos((it as AParam).identifier); addVar((it as AParam).identifier, Helper.getTypeFromPType(it.type), true)}
    }
    override fun outAFunctiondcl(node: AFunctiondcl) {
        closeScope()
    }

    override fun inATemplateModuledcl(node: ATemplateModuledcl) {
        openScope()

        val name = node.identifier.text
        currentVarPrefix = "$name->"

        // Add each parameter variable to the scope
        node.param.forEach {errorHandler.setLineAndPos((it as AParam).identifier); addVar((it as AParam).identifier, Helper.getTypeFromPType(it.type), true)}
    }

    override fun outATemplateModuledcl(node: ATemplateModuledcl) {
        closeScope()
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

    override fun caseAStopStmt(node: AStopStmt) {
        val name = node.identifier?.text
        if (name != null && name !in moduleTable) {
            error(IdentifierNotDeclaredError("No module with name $name exists.\nCandidates are: ${moduleTable.keys.joinToString(", ")}"))
        }
    }
}
