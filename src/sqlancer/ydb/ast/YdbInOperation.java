package sqlancer.ydb.ast;

import sqlancer.ydb.YdbType;

import java.util.List;

public class YdbInOperation implements YdbExpression {

    private final YdbExpression expr;
    private final List<YdbExpression> listElements;
    private final boolean isTrue;

    public YdbInOperation(YdbExpression expr, List<YdbExpression> listElements, boolean isTrue) {
        this.expr = expr;
        this.listElements = listElements;
        this.isTrue = isTrue;
    }

    public YdbExpression getExpr() {
        return expr;
    }

    public List<YdbExpression> getListElements() {
        return listElements;
    }

    @Override
    public YdbConstant getExpectedValue() {
        YdbConstant leftValue = expr.getExpectedValue();
        if (leftValue == null) {
            return null;
        }
        if (leftValue.isNull()) {
            return YdbConstant.createNullConstant();
        }
        boolean isNull = false;
        for (YdbExpression expr : getListElements()) {
            YdbConstant rightExpectedValue = expr.getExpectedValue();
            if (rightExpectedValue == null) {
                return null;
            }
            if (rightExpectedValue.isNull()) {
                isNull = true;
            } else if (rightExpectedValue.isEquals(this.expr.getExpectedValue()).isBoolean()
                    && rightExpectedValue.isEquals(this.expr.getExpectedValue()).asBoolean()) {
                return YdbConstant.createBooleanConstant(isTrue);
            }
        }

        if (isNull) {
            return YdbConstant.createNullConstant();
        } else {
            return YdbConstant.createBooleanConstant(!isTrue);
        }
    }

    public boolean isTrue() {
        return isTrue;
    }

    @Override
    public YdbType getExpressionType() {
        return YdbType.bool();
    }
}
