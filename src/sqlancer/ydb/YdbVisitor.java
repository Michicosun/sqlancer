package sqlancer.ydb;

import sqlancer.ydb.ast.*;
import sqlancer.ydb.ast.YdbSelect.YdbFromTable;
import sqlancer.ydb.ast.YdbSelect.YdbSubquery;
import sqlancer.ydb.YdbProvider.YdbGlobalState;
import sqlancer.ydb.gen.YdbExpressionGenerator;

import java.util.List;

public interface YdbVisitor {

    void visit(YdbConstant constant);

    void visit(YdbColumnValue c);

    void visit(YdbPrefixOperation op);
    
    void visit(YdbPostfixOperation op);

    void visit(YdbSelect op);

    void visit(YdbOrderByTerm op);

    void visit(YdbFunction f);

    void visit(YdbCastOperation cast);

    void visit(YdbInOperation op);

    void visit(YdbAggregate op);

    void visit(YdbFromTable from);

    void visit(YdbSubquery subquery);

    void visit(YdbBinaryLogicalOperation op);

    void visit(YdbLikeOperation op);

    default void visit(YdbExpression expression) {
        if (expression instanceof YdbConstant) {
            visit((YdbConstant) expression);
        } else if (expression instanceof YdbPostfixOperation) {
            visit((YdbPostfixOperation) expression);
        } else if (expression instanceof YdbColumnValue) {
            visit((YdbColumnValue) expression);
        } else if (expression instanceof YdbPrefixOperation) {
            visit((YdbPrefixOperation) expression);
        } else if (expression instanceof YdbSelect) {
            visit((YdbSelect) expression);
        } else if (expression instanceof YdbOrderByTerm) {
            visit((YdbOrderByTerm) expression);
        } else if (expression instanceof YdbFunction) {
            visit((YdbFunction) expression);
        } else if (expression instanceof YdbCastOperation) {
            visit((YdbCastOperation) expression);
        } else if (expression instanceof YdbInOperation) {
            visit((YdbInOperation) expression);
        } else if (expression instanceof YdbAggregate) {
            visit((YdbAggregate) expression);
        } else if (expression instanceof YdbFromTable) {
            visit((YdbFromTable) expression);
        } else if (expression instanceof YdbSubquery) {
            visit((YdbSubquery) expression);
        } else if (expression instanceof YdbLikeOperation) {
            visit((YdbLikeOperation) expression);
        } else {
            throw new AssertionError(expression);
        }
    }

    static String asString(YdbExpression expr) {
        YdbToStringVisitor visitor = new YdbToStringVisitor();
        visitor.visit(expr);
        return visitor.get();
    }

    static String getExpressionAsString(YdbGlobalState globalState, YdbType type, List<YdbSchema.YdbColumn> columns) {
        YdbExpression expression = YdbExpressionGenerator.generateExpression(globalState, columns, type);
        YdbToStringVisitor visitor = new YdbToStringVisitor();
        visitor.visit(expression);
        return visitor.get();
    }

}