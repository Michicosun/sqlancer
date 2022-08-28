package sqlancer.ydb.ast;

import sqlancer.common.ast.BinaryNode;
import sqlancer.ydb.YdbType;

public class YdbConcatOperation extends BinaryNode<YdbExpression> implements YdbExpression {

    public YdbConcatOperation(YdbExpression left, YdbExpression right) {
        super(left, right);
    }

    @Override
    public YdbType getExpressionType() {
        return YdbType.string();
    }

    @Override
    public YdbConstant getExpectedValue() {
        YdbConstant leftExpectedValue = getLeft().getExpectedValue();
        YdbConstant rightExpectedValue = getRight().getExpectedValue();
        if (leftExpectedValue == null || rightExpectedValue == null) {
            return null;
        }
        if (leftExpectedValue.isNull() || rightExpectedValue.isNull()) {
            return YdbConstant.createNullConstant();
        }
        String leftStr = leftExpectedValue.cast(YdbType.string()).getUnquotedTextRepresentation();
        String rightStr = rightExpectedValue.cast(YdbType.string()).getUnquotedTextRepresentation();
        return YdbConstant.createStringConstant(leftStr + rightStr);
    }

    @Override
    public String getOperatorRepresentation() {
        return "||";
    }

}
