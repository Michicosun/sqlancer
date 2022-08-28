package sqlancer.ydb.ast;

import sqlancer.LikeImplementationHelper;
import sqlancer.common.ast.BinaryNode;
import sqlancer.ydb.YdbType;

public class YdbLikeOperation extends BinaryNode<YdbExpression> implements YdbExpression {

    public YdbLikeOperation(YdbExpression left, YdbExpression right) {
        super(left, right);
    }

    @Override
    public YdbType getExpressionType() {
        return YdbType.bool();
    }

    @Override
    public YdbConstant getExpectedValue() {
        YdbConstant leftVal = getLeft().getExpectedValue();
        YdbConstant rightVal = getRight().getExpectedValue();
        if (leftVal == null || rightVal == null) {
            return null;
        }
        if (leftVal.isNull() || rightVal.isNull()) {
            return YdbConstant.createNullConstant();
        } else {
            boolean val = LikeImplementationHelper.match(leftVal.asString(), rightVal.asString(), 0, 0, true);
            return YdbConstant.createBooleanConstant(val);
        }
    }

    @Override
    public String getOperatorRepresentation() {
        return "LIKE";
    }

}