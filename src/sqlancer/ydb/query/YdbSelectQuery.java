package sqlancer.ydb.query;

import com.yandex.ydb.table.query.DataQueryResult;
import sqlancer.GlobalState;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.ydb.YdbConnection;
import sqlancer.ydb.YdbQueryAdapter;
import sqlancer.ydb.ast.YdbSelect;

public class YdbSelectQuery extends YdbQueryAdapter {

    DataQueryResult resultRows;
    YdbSelect selectQuery;

    public YdbSelectQuery(YdbSelect selectQuery) {
        this.selectQuery = selectQuery;
    }

    public DataQueryResult getResultSet() {
        return resultRows;
    }

    @Override
    public String getLogString() {
        return null;
    }

    @Override
    public boolean couldAffectSchema() {
        return false;
    }

    @Override
    public <G extends GlobalState<?, ?, YdbConnection>> boolean execute(G globalState, String... fills) throws Exception {
        return false;
    }

    @Override
    public ExpectedErrors getExpectedErrors() {
        return null;
    }
}
