package sqlancer.ydb.ast;

import sqlancer.Randomly;
import sqlancer.ydb.YdbType;

public class YdbOrderByTerm implements YdbExpression {

    private final YdbOrder order;
    private final YdbExpression expr;

    public enum YdbOrder {
        ASC, DESC;

        public static YdbOrder getRandomOrder() {
            return Randomly.fromOptions(YdbOrder.values());
        }
    }

    public YdbOrderByTerm(YdbExpression expr, YdbOrder order) {
        this.expr = expr;
        this.order = order;
    }

    public YdbOrder getOrder() {
        return order;
    }

    public YdbExpression getExpr() {
        return expr;
    }

    @Override
    public YdbConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    @Override
    public YdbType getExpressionType() {
        return null;
    }

}
