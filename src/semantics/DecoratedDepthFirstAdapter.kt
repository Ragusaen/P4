package semantics

import sablecc.analysis.DepthFirstAdapter

class DecoratedDepthFirstAdapter : DepthFirstAdapter() {
    fun inIntToFloatConversionNode(node:IntToFloatConversionNode) = defaultIn(node)
    fun outIntToFloatConversionNode(node:IntToFloatConversionNode) = defaultOut(node)

//    @Override
//    public void caseAIdentifierValue(AIdentifierValue node)
//    {
//        inAIdentifierValue(node);
//        if(node.getIdentifier() != null)
//        {
//            node.getIdentifier().apply(this);
//        }
//        outAIdentifierValue(node);
//    }

    fun caseIntToFloatConversion(node:IntToFloatConversionNode) {
        inIntToFloatConversionNode(node)
        if (node)
        outIntToFloatConversionNode(node)
    }
}