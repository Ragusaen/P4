package semantics

import sablecc.analysis.Analysis
import sablecc.node.*

class IntToFloatConversionNode(val child: PExpr) : PExpr() {
    override fun apply(sw: Switch?) {
        DecoratedDepthFirstAdapter().caseIntToFloatConversion(this);
    }

    override fun clone(): Any {
        return IntToFloatConversionNode(cloneNode(child))
    }
}