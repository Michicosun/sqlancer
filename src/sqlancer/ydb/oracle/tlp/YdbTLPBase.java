package sqlancer.ydb.oracle.tlp;

import sqlancer.Randomly;
import sqlancer.common.gen.ExpressionGenerator;
import sqlancer.common.oracle.TernaryLogicPartitioningOracleBase;
import sqlancer.common.oracle.TestOracle;
import sqlancer.ydb.YdbProvider.YdbGlobalState;
import sqlancer.ydb.YdbSchema;
import sqlancer.ydb.YdbSchema.YdbColumn;
import sqlancer.ydb.YdbSchema.YdbTable;
import sqlancer.ydb.YdbSchema.YdbTables;
import sqlancer.ydb.YdbType;
import sqlancer.ydb.ast.*;
import sqlancer.ydb.ast.YdbSelect.YdbFromTable;
import sqlancer.ydb.ast.YdbSelect.YdbSubquery;
import sqlancer.ydb.gen.YdbExpressionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class YdbTLPBase extends TernaryLogicPartitioningOracleBase<YdbExpression, YdbGlobalState> implements TestOracle {

    protected YdbSchema s;
    protected YdbTables targetTables;
    protected YdbExpressionGenerator gen;
    protected YdbSelect select;

    public YdbTLPBase(YdbGlobalState state) {
        super(state);
//        YdbCommon.addCommonExpressionErrors(errors);
//        YdbCommon.addCommonFetchErrors(errors);
    }

    @Override
    public void check() throws Exception {
        s = state.getSchema();
        targetTables = s.getRandomTableNonEmptyTables();
        List<YdbTable> tables = targetTables.getTables();
        List<YdbJoin> joins = Collections.emptyList();
//        List<YdbJoin> joins = getJoinStatements(state, targetTables.getColumns(), tables);
        generateSelectBase(tables, joins);
    }

    protected List<YdbJoin> getJoinStatements(YdbGlobalState globalState, List<YdbColumn> columns,
            List<YdbTable> tables) {
//        return YdbNoRECOracle.getJoinStatements(state, columns, tables);
        return null;
    }

    protected void generateSelectBase(List<YdbTable> tables, List<YdbJoin> joins) {
        List<YdbExpression> tableList = tables.stream().map(t -> new YdbFromTable(t)).collect(Collectors.toList());
        gen = new YdbExpressionGenerator(state).setColumns(targetTables.getColumns());
        initializeTernaryPredicateVariants();
        select = new YdbSelect();
        select.setFetchColumns(generateFetchColumns());
        select.setFromList(tableList);
        select.setJoinClauses(joins);
        select.setWhereClause(null);
    }

    List<YdbExpression> generateFetchColumns() {
        if (Randomly.getBooleanWithRatherLowProbability()) {
            return Arrays.asList(new YdbColumnValue(YdbColumn.createDummy("*"), null));
        }
        List<YdbExpression> fetchColumns = new ArrayList<>();
        List<YdbColumn> targetColumns = Randomly.nonEmptySubset(targetTables.getColumns());
        for (YdbColumn c : targetColumns) {
            fetchColumns.add(new YdbColumnValue(c, null));
        }
        return fetchColumns;
    }

    @Override
    protected ExpressionGenerator<YdbExpression> getGen() {
        return gen;
    }

    public static YdbSubquery createSubquery(YdbGlobalState globalState, String name, YdbTables tables) {
        List<YdbExpression> columns = new ArrayList<>();
        YdbExpressionGenerator gen = new YdbExpressionGenerator(globalState).setColumns(tables.getColumns());
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            columns.add(gen.generateExpression(0));
        }
        YdbSelect select = new YdbSelect();
        select.setFromList(tables.getTables().stream().map(t -> new YdbFromTable(t))
                .collect(Collectors.toList()));
        select.setFetchColumns(columns);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(0, YdbType.bool()));
        }
        if (Randomly.getBooleanWithRatherLowProbability()) {
            select.setOrderByExpressions(gen.generateOrderBy());
        }
        if (Randomly.getBoolean()) {
            select.setLimitClause(YdbConstant.createUInt32Constant(Randomly.getPositiveOrZeroNonCachedInteger()));
            if (Randomly.getBoolean()) {
                select.setOffsetClause(
                        YdbConstant.createUInt32Constant(Randomly.getPositiveOrZeroNonCachedInteger()));
            }
        }
        return new YdbSubquery(select, name);
    }

}
