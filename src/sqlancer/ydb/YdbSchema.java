package sqlancer.ydb;

import com.yandex.ydb.scheme.SchemeOperationProtos;
import com.yandex.ydb.table.SchemeClient;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.ListDirectoryResult;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcSchemeRpc;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.values.Type;
import sqlancer.common.schema.*;

import sqlancer.ydb.YdbProvider.YdbGlobalState;
import sqlancer.ydb.YdbSchema.YdbTable;

import java.util.*;

public class YdbSchema extends AbstractSchema<YdbGlobalState, YdbTable> {

    public YdbSchema(List<YdbTable> databaseTables) {
        super(databaseTables);
    }

    public YdbSchema(YdbConnection con, String databasePath) {
        super(YdbSchema.getDatabaseTables(con, databasePath));
    }

    public static class YdbColumn extends AbstractTableColumn<YdbTable, YdbType> {

        public YdbColumn(String name, YdbTable table, YdbType type) {
            super(name, table, type);
        }

        public YdbColumn(String name, YdbType type) {
            super(name, null, type);
        }

        public static YdbColumn createDummy(String name) {
            return new YdbColumn(name, YdbType.getRandom());
        }
        
    }

    public static class YdbTable extends AbstractTable<YdbColumn, TableIndex, YdbGlobalState> {

        String fullPath;
        String dbPath;

        public YdbTable(String fullPath, String dbPath, List<YdbColumn> columns, List<TableIndex> indexes, boolean isView) {
            super(dbPath, columns, indexes, isView);
            this.fullPath = fullPath;
            this.dbPath = dbPath;
        }

        public String getFullPath() {
            return fullPath;
        }

        public String getDbPath() {
            return dbPath;
        }

        @Override
        public long getNrRows(YdbGlobalState globalState) {
            throw new UnsupportedOperationException();
        }
    }

    public static class YdbTables extends AbstractTables<YdbTable, YdbColumn> {
        public YdbTables(List<YdbTable> tables) {
            super(tables);
        }
    }

    public static String cropLastDir(String path) {
        int lastDel = 0;
        for (int i = 0; i < path.length(); ++i) {
            if (path.charAt(i) == '/') {
                lastDel = i;
            }
        }
        return path.substring(lastDel + 1);
    }

    private static List<YdbTable> getDatabaseTables(YdbConnection con, String root) {
        List<YdbTable> databaseTables = new ArrayList<>();
        List<String> tableFullPathes = getTableNames(con, root);
        for (String fullPath : tableFullPathes) {
            List<YdbColumn> databaseColumns = getTableColumns(con, fullPath);
            List<TableIndex> indexes = Collections.emptyList();
            YdbTable t = new YdbTable(fullPath, cropLastDir(fullPath), databaseColumns, indexes, false);
            for (YdbColumn c : databaseColumns) {
                c.setTable(t);
            }
            databaseTables.add(t);
        }
        return databaseTables;
    }

    private static List<String> getTableNames(YdbConnection con, String root) {
        List<String> tableNames = new ArrayList<>();
        Stack<String> dirs = new Stack<>();
        dirs.push(root);
        try (SchemeClient client = SchemeClient.newClient(GrpcSchemeRpc.useTransport(con.transport)).build()) {
            while (!dirs.empty()) {
                String dir = dirs.peek();
                dirs.pop();

                ListDirectoryResult listResult = client.listDirectory(dir).join().expect("list directory error");
                for (SchemeOperationProtos.Entry child : listResult.getChildren()) {
                    String entryName = dir + "/" + child.getName();
                    if (child.getType() == SchemeOperationProtos.Entry.Type.DIRECTORY) {
                        dirs.push(entryName);
                    } else if (child.getType() == SchemeOperationProtos.Entry.Type.TABLE) {
                        tableNames.add(entryName);
                    }
                }
            }
        }
        return tableNames;
    }

    private static List<YdbColumn> getTableColumns(YdbConnection con, String tableName) {
        List<YdbColumn> columns = new ArrayList<>();
        try (TableClient client = TableClient.newClient(GrpcTableRpc.useTransport(con.transport)).build()) {
            SessionRetryContext context = SessionRetryContext.create(client).build();

            TableDescription description = context.supplyResult(session -> {
                return session.describeTable(tableName);
            }).join().expect("describe table error");

            for (TableColumn innerColumn : description.getColumns()) {
                String name = innerColumn.getName();
                Type type = innerColumn.getType();
                columns.add(new YdbColumn(name, null, new YdbType(type)));
            }
        }
        return columns;
    }

}
