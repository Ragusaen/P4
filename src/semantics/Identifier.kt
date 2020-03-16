package semantics

import sablecc.node.Node

open class Identifier(var type:String, var value:Any? = null, var nodeRef:Node? = null) {

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        else if (other !is Identifier)
            return false
        return other.type == this.type && other.value == this.value && other.nodeRef == this.nodeRef
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
