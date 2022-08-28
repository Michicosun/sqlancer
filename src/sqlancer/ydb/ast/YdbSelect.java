package sqlancer.ydb.ast;

import sqlancer.Randomly;
import sqlancer.common.ast.SelectBase;
import sqlancer.ydb.YdbSchema.YdbTable;
import sqlancer.ydb.YdbType;

import java.util.Collections;
import java.util.List;

public class YdbSelect extends SelectBase<YdbExpression> implements YdbExpression {

    private SelectType selectOption = SelectType.ALL;
    private List<YdbJoin> joinClauses = Collections.emptyList();
    private YdbExpression distinctOnClause;

    public static class YdbFromTable implements YdbExpression {
        private final YdbTable t;

        public YdbFromTable(YdbTable t) {
            this.t = t;
        }

        public YdbTable getTable() {
            return t;
        }

        @Override
        public YdbType getExpressionType() {
            return null;
        }
    }

    public static class YdbSubquery implements YdbExpression {
        private final YdbSelect s;
        private final String name;

        public YdbSubquery(YdbSelect s, String name) {
            this.s = s;
            this.name = name;
        }

        public YdbSelect getSelect() {
            return s;
        }

        public String getName() {
            return name;
        }

        @Override
        public YdbType getExpressionType() {
            return null;
        }
    }

    public enum SelectType {
        DISTINCT, ALL;

        public static SelectType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public void setSelectType(SelectType fromOptions) {
        this.setSelectOption(fromOptions);
    }

    public void setDistinctOnClause(YdbExpression distinctOnClause) {
        if (selectOption != SelectType.DISTINCT) {
            throw new IllegalArgumentException();
        }
        this.distinctOnClause = distinctOnClause;
    }

    public SelectType getSelectOption() {
        return selectOption;
    }

    public void setSelectOption(SelectType fromOptions) {
        this.selectOption = fromOptions;
    }

    @Override
    public YdbType getExpressionType() {
        return null;
    }

    public void setJoinClauses(List<YdbJoin> joinStatements) {
        this.joinClauses = joinStatements;

    }

    public List<YdbJoin> getJoinClauses() {
        return joinClauses;
    }

    public YdbExpression getDistinctOnClause() {
        return distinctOnClause;
    }

}
