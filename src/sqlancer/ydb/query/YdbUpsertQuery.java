package sqlancer.ydb.query;

import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import sqlancer.GlobalState;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.ydb.YdbConnection;
import sqlancer.ydb.YdbQueryAdapter;

public class YdbUpsertQuery extends YdbQueryAdapter {

    String upsertDataQuery;
    ExpectedErrors errors;

    public YdbUpsertQuery(String dataQuery, ExpectedErrors errors) {
        this.upsertDataQuery = dataQuery;
        this.errors = errors;
    }

    @Override
    public String getLogString() {
        return upsertDataQuery;
    }

    @Override
    public <G extends GlobalState<?, ?, YdbConnection>> boolean execute(G globalState, String... fills) throws Exception {
        boolean upsertStatus = true;
        try (TableClient client = TableClient.newClient(GrpcTableRpc.useTransport(globalState.getConnection().transport)).build()) {
            SessionRetryContext ctx = SessionRetryContext.create(client).build();
            DataQueryResult result = ctx.supplyResult(session -> {
                return session.executeDataQuery(upsertDataQuery, TxControl.serializableRw().setCommitTx(true));
            }).join().expect("create table error");
        } catch (Exception e) {
            upsertStatus = false;
            e.printStackTrace();
        }
        return upsertStatus;
    }

    @Override
    public boolean couldAffectSchema() { return false; }

    @Override
    public ExpectedErrors getExpectedErrors() { return errors; }

}
