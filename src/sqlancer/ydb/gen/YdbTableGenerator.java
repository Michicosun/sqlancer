package sqlancer.ydb.gen;

import com.beust.ah.A;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.values.Type;
import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.ydb.YdbProvider;
import sqlancer.ydb.YdbProvider.YdbGlobalState;
import sqlancer.ydb.YdbQueryAdapter;
import sqlancer.ydb.YdbSchema;
import sqlancer.ydb.YdbSchema.YdbColumn;
import sqlancer.ydb.YdbSchema.YdbTable;
import sqlancer.ydb.YdbType;
import sqlancer.ydb.query.YdbCreateTableQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YdbTableGenerator {
    private YdbTable table;
    private final List<YdbSchema.YdbColumn> columnsToBeAdded = new ArrayList<>();

    private static ExpectedErrors errors;

    static {
        errors = ExpectedErrors.from();
    }

    public YdbQueryAdapter getQuery(YdbGlobalState globalState) {
        String dbPath = globalState.getSchema().getFreeTableName();
        String fullPath = globalState.getDatabaseName() + "/" + dbPath;
        TableDescription.Builder builder = TableDescription.newBuilder();
        table = new YdbTable(fullPath, dbPath, columnsToBeAdded, Collections.emptyList(), false);

        int tableColumnSize = Randomly.smallNumber() + 1;

        YdbColumn col = createColumn(String.format("c%d", 0), true);
        builder.addNullableColumn(col.getName(), col.getType().getYdbType());

        for (int i = 1; i < tableColumnSize; i++) {
            col = createColumn(String.format("c%d", i), false);
            builder.addNullableColumn(col.getName(), col.getType().getYdbType());
        }

        List<YdbColumn> canBePrimary = new ArrayList<>();
        for (YdbColumn c : columnsToBeAdded) {
            if (YdbType.canBePrimary(c.getType().getYdbType())) {
                canBePrimary.add(c);
            }
        }

        List<YdbColumn> primaryColumns = Randomly.nonEmptySubset(canBePrimary);
        List<String> primaryNames = new ArrayList<>();
        for (YdbColumn c : primaryColumns) {
            primaryNames.add(c.getName());
        }

        builder.setPrimaryKeys(primaryNames);

        return new YdbCreateTableQuery(fullPath, builder.build(), errors);
    }

    private YdbColumn createColumn(String columnName, boolean mustBePrimary) {
        YdbType columnType = YdbType.getRandomColumnType(mustBePrimary);
        YdbColumn newColumn = new YdbColumn(columnName, columnType);
        newColumn.setTable(table);
        columnsToBeAdded.add(newColumn);
        return newColumn;
    }

    public String getTableName() {
        return table.getName();
    }

    public YdbTable getGeneratedTable() {
        return table;
    }
}
