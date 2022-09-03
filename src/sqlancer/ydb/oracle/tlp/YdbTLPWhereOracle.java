package sqlancer.ydb.oracle.tlp;

import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.values.Value;
import sqlancer.Randomly;
import sqlancer.ydb.YdbComparatorHelper;
import sqlancer.ydb.YdbProvider.YdbGlobalState;
import sqlancer.ydb.YdbVisitor;
import sqlancer.ydb.query.YdbSelectQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YdbTLPWhereOracle extends YdbTLPBase {

    public YdbTLPWhereOracle(YdbGlobalState state) {
        super(state);
    }

    @Override
    public void check() throws Exception {
        super.check();
        whereCheck();
    }

    protected void whereCheck() throws Exception {
//        YdbSelectQuery adapter = new YdbSelectQuery(select);
        System.out.println("original: " + YdbVisitor.asString(select));
//        List<List<Value<?>>> fullResultSet = YdbComparatorHelper.getResultSet(adapter, state);

//        List<List<Value<?>>> compoundResultSet = new ArrayList<>();

        select.setOrderByClause(Collections.emptyList());

        select.setWhereClause(predicate);
        System.out.println("predicate: " + YdbVisitor.asString(select));
//        compoundResultSet.addAll(YdbComparatorHelper.getResultSet(adapter, state));

        select.setWhereClause(negatedPredicate);
        System.out.println("negatedPredicate: " + YdbVisitor.asString(select));
//        compoundResultSet.addAll(YdbComparatorHelper.getResultSet(adapter, state));

        select.setWhereClause(isNullPredicate);
        System.out.println("nullPredicate: " + YdbVisitor.asString(select));
//        compoundResultSet.addAll(YdbComparatorHelper.getResultSet(adapter, state));

//        select.setWhereClause(predicate);
//        YdbComparatorHelper.assumeResultSetsAreEqual(fullResultSet, compoundResultSet, adapter);
    }
}
