package sqlancer.ydb.ast;

import sqlancer.ydb.YdbSchema.YdbColumn;
import sqlancer.ydb.YdbType;

public class YdbColumnValue implements YdbExpression {

    private final YdbColumn c;
    private final YdbConstant expectedValue;

    public YdbColumnValue(YdbColumn c, YdbConstant expectedValue) {
        this.c = c;
        this.expectedValue = expectedValue;
    }

    @Override
    public YdbType getExpressionType() {
        return c.getType();
    }

    @Override
    public YdbConstant getExpectedValue() {
        return expectedValue;
    }

    public static YdbColumnValue create(YdbColumn c, YdbConstant expected) {
        return new YdbColumnValue(c, expected);
    }

    public YdbColumn getColumn() {
        return c;
    }

}
