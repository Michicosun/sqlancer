package sqlancer.ydb.ast;

import sqlancer.ydb.YdbType;

public interface YdbExpression {

    default YdbType getExpressionType() { return null; }

    default YdbConstant getExpectedValue() { return null; }

}
