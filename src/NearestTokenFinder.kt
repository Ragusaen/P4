import sablecc.node.Node
import sablecc.node.Token

class NearestTokenFinder : ErrorTraverser(ErrorHandler("")) {
    fun traverseFrom(node: Node): Token? {
        node.apply(this)
        return errorHandler.lastToken
    }
}

fun getNearestToken(node: Node): Token? = NearestTokenFinder().traverseFrom(node)