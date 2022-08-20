package sqlancer.ydb.query;

import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import sqlancer.GlobalState;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.ydb.YdbConnection;
import sqlancer.ydb.YdbQueryAdapter;

public class YdbCreateTableQuery extends YdbQueryAdapter {

    String fullPath;
    TableDescription tableDesc;
    ExpectedErrors errors;

    public YdbCreateTableQuery(String tablePath, TableDescription tableDesc, ExpectedErrors errors) {
        this.fullPath = tablePath;
        this.tableDesc = tableDesc;
        this.errors = errors;
    }

    @Override
    public String getLogString() {
        return fullPath + " -> " + tableDesc.toString();
    }

    @Override
    public <G extends GlobalState<?, ?, YdbConnection>> boolean execute(G globalState, String... fills) throws Exception {
        boolean createStatus = true;
        try (TableClient client = TableClient.newClient(GrpcTableRpc.useTransport(globalState.getConnection().transport)).build()) {
            SessionRetryContext ctx = SessionRetryContext.create(client).build();
            ctx.supplyStatus(session -> {
                return session.createTable(fullPath, tableDesc);
            }).join().expect("create table error");
        } catch (Exception e) {
            createStatus = false;
            e.printStackTrace();
        }
        return createStatus;
    }

    @Override
    public boolean couldAffectSchema() { return true; }

    @Override
    public ExpectedErrors getExpectedErrors() { return errors; }
}
