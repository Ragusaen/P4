package semantics.TypeChecking

import sablecc.node.*
import semantics.DecoratedDepthFirstAdapter

class IntToFloatConversionNode(val child: PExpr) : PExpr() {
    override fun apply(sw: Switch?) {
        DecoratedDepthFirstAdapter().caseIntToFloatConversion(this);
    }

    override fun clone(): Any {
        return IntToFloatConversionNode(cloneNode(child))
    }
}