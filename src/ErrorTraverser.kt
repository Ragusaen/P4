import sablecc.analysis.DepthFirstAdapter
import sablecc.node.*

abstract class ErrorTraverser(val errorHandler: ErrorHandler) : DepthFirstAdapter() {

    fun error(ce: CompileError): Nothing = errorHandler.compileError(ce)

    override fun caseTAssign(node: TAssign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTStop(node: TStop){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTOr(node: TOr){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTLbrace(node: TLbrace){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTLbracket(node: TLbracket){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTStringtype(node: TStringtype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTGreaterthan(node: TGreaterthan){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTSubtraction(node: TSubtraction){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDigitalinputpintype(node: TDigitalinputpintype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTNotequal(node: TNotequal){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTContinue(node: TContinue){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDivision(node: TDivision){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAnaloginputpintype(node: TAnaloginputpintype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTBooltype(node: TBooltype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTModulo(node: TModulo){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTMultiplication(node: TMultiplication){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAnalogoutputpintype(node: TAnalogoutputpintype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTReturn(node: TReturn){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAnd(node: TAnd){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDivisionassign(node: TDivisionassign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTFloattype(node: TFloattype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTTimeliteral(node: TTimeliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTColon(node: TColon){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDigitalpinliteral(node: TDigitalpinliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTSubtractionassign(node: TSubtractionassign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTRead(node: TRead){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDigitaloutputpintype(node: TDigitaloutputpintype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTIntliteral(node: TIntliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTLparen(node: TLparen){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTFor(node: TFor){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTStart(node: TStart){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTTemplate(node: TTemplate){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTRbracket(node: TRbracket){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTStringliteral(node: TStringliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAdditionassign(node: TAdditionassign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTElse(node: TElse){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTLessthanorequalto(node: TLessthanorequalto){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAddition(node: TAddition){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTLessthan(node: TLessthan){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTInit(node: TInit){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTComment(node: TComment){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTModule(node: TModule){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTTimetype(node: TTimetype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTTo(node: TTo){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTEqual(node: TEqual){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTModuloassign(node: TModuloassign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTRbrace(node: TRbrace){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTDelay(node: TDelay){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTIdentifier(node: TIdentifier){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTEol(node: TEol){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTFloatliteral(node: TFloatliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTOn(node: TOn){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTSet(node: TSet){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTEvery(node: TEvery){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTMultiplicationassign(node: TMultiplicationassign){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTComma(node: TComma){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTWhile(node: TWhile){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTFun(node: TFun){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTGreaterthanorequalto(node: TGreaterthanorequalto){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTBoolliteral(node: TBoolliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTAnalogpinliteral(node: TAnalogpinliteral){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTNot(node: TNot){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTInttype(node: TInttype){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTIf(node: TIf){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTBreak(node: TBreak){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTRparen(node: TRparen){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTUntil(node: TUntil){
        errorHandler.setLineAndPos(node)
    }

    override fun caseTWhitespace(node: TWhitespace){
        errorHandler.setLineAndPos(node)
    }


}