package sqlancer.ydb.ast;

import sqlancer.common.visitor.UnaryOperation;

public class YdbAlias implements UnaryOperation<YdbExpression>, YdbExpression {

    private final YdbExpression expr;
    private final String alias;

    public YdbAlias(YdbExpression expr, String alias) {
        this.expr = expr;
        this.alias = alias;
    }

    @Override
    public YdbExpression getExpression() {
        return expr;
    }

    @Override
    public String getOperatorRepresentation() {
        return " as " + alias;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.POSTFIX;
    }

    @Override
    public boolean omitBracketsWhenPrinting() {
        return true;
    }

}
