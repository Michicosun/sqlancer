package sqlancer.ydb.ast;

import sqlancer.Randomly;
import sqlancer.ydb.YdbType;

public class YdbJoin implements YdbExpression {

    public enum YdbJoinType {
        INNER, LEFT, RIGHT, FULL, CROSS;

        public static YdbJoinType getRandom() {
            return Randomly.fromOptions(values());
        }

    }

    private final YdbExpression tableReference;
    private final YdbExpression onClause;
    private final YdbJoinType type;

    public YdbJoin(YdbExpression tableReference, YdbExpression onClause, YdbJoinType type) {
        this.tableReference = tableReference;
        this.onClause = onClause;
        this.type = type;
    }

    public YdbExpression getTableReference() {
        return tableReference;
    }

    public YdbExpression getOnClause() {
        return onClause;
    }

    public YdbJoinType getType() {
        return type;
    }

    @Override
    public YdbType getExpressionType() {
        throw new AssertionError();
    }

    @Override
    public YdbConstant getExpectedValue() {
        throw new AssertionError();
    }

}
