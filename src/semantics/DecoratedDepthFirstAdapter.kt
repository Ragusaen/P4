package semantics

import sablecc.analysis.DepthFirstAdapter

class DecoratedDepthFirstAdapter : DepthFirstAdapter() {
    fun inIntToFloatConversionNode(node:IntToFloatConversionNode) = defaultIn(node)
    fun outIntToFloatConversionNode(node:IntToFloatConversionNode) = defaultOut(node)
    fun caseIntToFloatConversion(node:IntToFloatConversionNode) {
        inIntToFloatConversionNode(node)
        node.child.apply(this)
        outIntToFloatConversionNode(node)
    }
}