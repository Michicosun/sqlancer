package sqlancer.ydb;

import sqlancer.Randomly;
import sqlancer.common.visitor.BinaryOperation;
import sqlancer.common.visitor.ToStringVisitor;
import sqlancer.ydb.ast.*;
import sqlancer.ydb.ast.YdbSelect.YdbFromTable;
import sqlancer.ydb.ast.YdbSelect.YdbSubquery;
import sqlancer.ydb.YdbType;

public final class YdbToStringVisitor extends ToStringVisitor<YdbExpression> implements YdbVisitor {

    @Override
    public void visitSpecific(YdbExpression expr) {
        YdbVisitor.super.visit(expr);
    }

    @Override
    public void visit(YdbConstant constant) {
        sb.append(constant.getTextRepresentation());
    }

    @Override
    public String get() {
        return sb.toString();
    }

    @Override
    public void visit(YdbColumnValue c) {
        YdbSchema.YdbColumn col = c.getColumn();
        if (col.getName().equals("*")) {
            sb.append("*");
        } else {
            YdbSchema.YdbTable table = col.getTable();
            sb.append("`");
            sb.append(table.getDbPath());
            sb.append("`.");
            sb.append(col.getName());
        }
    }

    @Override
    public void visit(YdbPrefixOperation op) {
        sb.append(op.getTextRepresentation());
        sb.append(" (");
        visit(op.getExpression());
        sb.append(")");
    }

    @Override
    public void visit(YdbFromTable from) {
        sb.append("`");
        sb.append(from.getTable().getDbPath());
        sb.append("`");
    }

    @Override
    public void visit(YdbSubquery subquery) {
        sb.append("(");
        visit(subquery.getSelect());
        sb.append(") AS ");
        sb.append(subquery.getName());
    }

    @Override
    public void visit(YdbSelect s) {
        sb.append("SELECT ");
        switch (s.getSelectOption()) {
        case DISTINCT:
            sb.append("DISTINCT ");
            break;
        }
        if (s.getFetchColumns() == null) {
            sb.append("*");
        } else {
            visit(s.getFetchColumns());
        }
        sb.append(" FROM ");
        visit(s.getFromList());

        for (YdbJoin j : s.getJoinClauses()) {
            sb.append(" ");
            switch (j.getType()) {
            case INNER:
                sb.append("INNER JOIN");
                break;
            case LEFT:
                sb.append("LEFT OUTER JOIN");
                break;
            case RIGHT:
                sb.append("RIGHT OUTER JOIN");
                break;
            case FULL:
                sb.append("FULL OUTER JOIN");
                break;
            case CROSS:
                sb.append("CROSS JOIN");
                break;
            default:
                throw new AssertionError(j.getType());
            }
            sb.append(" ");
            visit(j.getTableReference());
            if (j.getType() != YdbJoin.YdbJoinType.CROSS) {
                sb.append(" ON ");
                visit(j.getOnClause());
            }
        }

        if (s.getWhereClause() != null) {
            sb.append(" WHERE ");
            visit(s.getWhereClause());
        }
        if (s.getGroupByExpressions().size() > 0) {
            sb.append(" GROUP BY ");
            visit(s.getGroupByExpressions());
        }
        if (s.getHavingClause() != null) {
            sb.append(" HAVING ");
            visit(s.getHavingClause());
        }
        if (!s.getOrderByExpressions().isEmpty()) {
            sb.append(" ORDER BY ");
            visit(s.getOrderByExpressions());
        }
        if (s.getLimitClause() != null) {
            sb.append(" LIMIT ");
            visit(s.getLimitClause());
        }

        if (s.getOffsetClause() != null) {
            sb.append(" OFFSET ");
            visit(s.getOffsetClause());
        }
    }

    @Override
    public void visit(YdbOrderByTerm op) {
        visit(op.getExpr());
        sb.append(" ");
        sb.append(op.getOrder());
    }

    @Override
    public void visit(YdbFunction f) {
        sb.append(f.getFunctionName());
        sb.append("(");
        int i = 0;
        for (YdbExpression arg : f.getArguments()) {
            if (i++ != 0) {
                sb.append(", ");
            }
            visit(arg);
        }
        sb.append(")");
    }

    @Override
    public void visit(YdbCastOperation cast) {
        sb.append("CAST(");
        visit(cast.getExpression());
        sb.append(" AS ");
        appendType(cast);
        sb.append(")");
    }

    private void appendType(YdbCastOperation cast) {
        YdbType type = cast.getType();
        switch (type.typeClass) {
        case BOOL:
            sb.append("Bool");
            break;
        case INT8:
            sb.append("Int8");
            break;
        case INT16:
            sb.append("Int16");
            break;
        case INT32:
            sb.append("Int32");
            break;
        case INT64:
            sb.append("Int64");
            break;
        case UINT8:
            sb.append("Uint8");
            break;
        case UINT16:
            sb.append("Uint16");
            break;
        case UINT32:
            sb.append("Uint32");
            break;
        case UINT64:
            sb.append("Uint64");
            break;
        case FLOAT:
            sb.append("Float");
            break;
        case DOUBLE:
            sb.append("Double");
            break;
        case STRING:
            sb.append("String");
            break;
        default:
            throw new AssertionError(cast.getType());
        }
    }

    @Override
    public void visit(YdbInOperation op) {
        sb.append("(");
        visit(op.getExpr());
        sb.append(")");
        if (!op.isTrue()) {
            sb.append(" NOT");
        }
        sb.append(" IN (");
        visit(op.getListElements());
        sb.append(")");
    }

    @Override
    public void visit(YdbAggregate op) {
        sb.append(op.getFunction());
        sb.append("(");
        visit(op.getArgs());
        sb.append(")");
    }

    @Override
    public void visit(YdbPostfixOperation op) {
        sb.append("(");
        visit(op.getExpression());
        sb.append(")");
        sb.append(" ");
        sb.append(op.getOperatorTextRepresentation());
    }

    @Override
    public void visit(YdbBinaryLogicalOperation op) {
        super.visit((BinaryOperation<YdbExpression>) op);
    }

    @Override
    public void visit(YdbLikeOperation op) {
        super.visit((BinaryOperation<YdbExpression>) op);
    }

}
