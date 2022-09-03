package sqlancer.ydb;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import sqlancer.SQLancerDBConnection;
import yandex.cloud.sdk.auth.provider.IamTokenCredentialProvider;

public class YdbConnection implements SQLancerDBConnection {

    public String root;

    public GrpcTransport transport;

    public YdbConnection(YdbOptions options) {
        String connectionUrl = options.getConnectionURL();
        if (options.hasToken()) {
            this.transport = GrpcTransport.forConnectionString(connectionUrl)
                                .withAuthProvider(CloudAuthProvider.newAuthProvider(
                                        IamTokenCredentialProvider.builder().token(options.accessToken).build()
                                ))
                                .build();
        } else {
            this.transport = GrpcTransport.forConnectionString(connectionUrl).build();
        }
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
