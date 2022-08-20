package sqlancer.ydb;

import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import sqlancer.SQLancerDBConnection;

public class YdbConnection implements SQLancerDBConnection {

    public String root;

    public GrpcTransport transport;

    public YdbConnection(String connectionURL) {
        this.transport = GrpcTransport.forConnectionString(connectionURL).build();
        this.root = transport.getDatabase();
    }

    @Override
    public String getDatabaseVersion() throws Exception {
        return transport.toString();
    }

    @Override
    public void close() throws Exception {
        transport.close();
    }
}
