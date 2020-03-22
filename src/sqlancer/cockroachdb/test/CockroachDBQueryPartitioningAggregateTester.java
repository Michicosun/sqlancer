package sqlancer.cockroachdb.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.postgresql.util.PSQLException;

import sqlancer.DatabaseProvider;
import sqlancer.IgnoreMeException;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.TestOracle;
import sqlancer.cockroachdb.CockroachDBCommon;
import sqlancer.cockroachdb.CockroachDBErrors;
import sqlancer.cockroachdb.CockroachDBProvider.CockroachDBGlobalState;
import sqlancer.cockroachdb.CockroachDBSchema;
import sqlancer.cockroachdb.CockroachDBSchema.CockroachDBDataType;
import sqlancer.cockroachdb.CockroachDBSchema.CockroachDBTables;
import sqlancer.cockroachdb.CockroachDBVisitor;
import sqlancer.cockroachdb.ast.CockroachDBAggregate;
import sqlancer.cockroachdb.ast.CockroachDBAggregate.CockroachDBAggregateFunction;
import sqlancer.cockroachdb.ast.CockroachDBAlias;
import sqlancer.cockroachdb.ast.CockroachDBCast;
import sqlancer.cockroachdb.ast.CockroachDBExpression;
import sqlancer.cockroachdb.ast.CockroachDBJoin;
import sqlancer.cockroachdb.ast.CockroachDBNotOperation;
import sqlancer.cockroachdb.ast.CockroachDBSelect;
import sqlancer.cockroachdb.ast.CockroachDBTableReference;
import sqlancer.cockroachdb.ast.CockroachDBUnaryPostfixOperation;
import sqlancer.cockroachdb.ast.CockroachDBUnaryPostfixOperation.CockroachDBUnaryPostfixOperator;
import sqlancer.cockroachdb.gen.CockroachDBExpressionGenerator;

public class CockroachDBQueryPartitioningAggregateTester implements TestOracle {

	private final CockroachDBGlobalState state;
	private final Set<String> errors = new HashSet<>();
	private CockroachDBExpressionGenerator gen;
	private String firstResult;
	private String secondResult;
	private String originalQuery;
	private String metamorphicQuery;

	public CockroachDBQueryPartitioningAggregateTester(CockroachDBGlobalState state) {
		this.state = state;
		CockroachDBErrors.addExpressionErrors(errors);
		errors.add("interface conversion: coldata.column");
		errors.add("float out of range");
	}

	public void check() throws SQLException {
		CockroachDBSchema s = state.getSchema();
		CockroachDBTables targetTables = s.getRandomTableNonEmptyTables();
		gen = new CockroachDBExpressionGenerator(state).setColumns(targetTables.getColumns());
		CockroachDBSelect select = new CockroachDBSelect();
		CockroachDBAggregateFunction windowFunction = Randomly
				.fromOptions(CockroachDBAggregate.CockroachDBAggregateFunction.getRandomMetamorphicOracle());
		CockroachDBAggregate aggregate = gen.generateArgsForAggregate(windowFunction.getRandomReturnType().get(), windowFunction);
		List<CockroachDBExpression> fetchColumns = new ArrayList<>();
		fetchColumns.add(aggregate);
		while (Randomly.getBooleanWithRatherLowProbability()) {
			fetchColumns.add(gen.generateAggregate());
		}
		select.setColumns(Arrays.asList(aggregate));
		List<CockroachDBTableReference> tableList = targetTables.getTables().stream()
				.map(t -> new CockroachDBTableReference(t)).collect(Collectors.toList());
		List<CockroachDBTableReference> from = CockroachDBCommon.getTableReferences(tableList);
		if (Randomly.getBooleanWithRatherLowProbability()) {
			select.setJoinList(CockroachDBNoRECTester.getJoins(from, state));
		}
		select.setFromTables(from);
		if (Randomly.getBooleanWithRatherLowProbability()) {
			select.setOrderByTerms(gen.getOrderingTerms());
		}
		originalQuery = CockroachDBVisitor.asString(select);
		updateStateString();
		firstResult = getAggregateResult(originalQuery);
		updateStateString();
		metamorphicQuery = createMetamorphicUnionQuery(select, aggregate, from);
		updateStateString();
		secondResult = getAggregateResult(metamorphicQuery);
		updateStateString();

		state.getState().queryString = "--" + originalQuery + ";\n--" + metamorphicQuery + "\n-- " + firstResult
				+ "\n-- " + secondResult;
		if (firstResult == null && secondResult != null
				|| firstResult != null && (!firstResult.contentEquals(secondResult)
						&& !DatabaseProvider.isEqualDouble(firstResult, secondResult))) {
			if (secondResult.contains("Inf")) {
				throw new IgnoreMeException(); // FIXME: average computation
			}
			throw new AssertionError();
		}

	}

	private String createMetamorphicUnionQuery(CockroachDBSelect select, CockroachDBAggregate aggregate,
			List<CockroachDBTableReference> from) {
		String metamorphicQuery;
		CockroachDBExpression whereClause = gen.generateExpression(CockroachDBDataType.BOOL.get());
		CockroachDBNotOperation negatedClause = new CockroachDBNotOperation(whereClause);
		CockroachDBUnaryPostfixOperation notNullClause = new CockroachDBUnaryPostfixOperation(whereClause,
				CockroachDBUnaryPostfixOperator.IS_NULL);
		List<CockroachDBExpression> mappedAggregate = mapped(aggregate);
		CockroachDBSelect leftSelect = getSelect(mappedAggregate, from, whereClause, select.getJoinList());
		CockroachDBSelect middleSelect = getSelect(mappedAggregate, from, negatedClause, select.getJoinList());
		CockroachDBSelect rightSelect = getSelect(mappedAggregate, from, notNullClause, select.getJoinList());
		metamorphicQuery = "SELECT " + getOuterAggregateFunction(aggregate).toString() + " FROM (";
		metamorphicQuery += CockroachDBVisitor.asString(leftSelect) + " UNION ALL "
				+ CockroachDBVisitor.asString(middleSelect) + " UNION ALL " + CockroachDBVisitor.asString(rightSelect);
		metamorphicQuery += ")";
		return metamorphicQuery;
	}

	private String getAggregateResult(String queryString) throws SQLException {
		String resultString;
		QueryAdapter q = new QueryAdapter(queryString, errors);
		try (ResultSet result = q.executeAndGet(state.getConnection())) {
			if (result == null) {
				throw new IgnoreMeException();
			}
			if (!result.next()) {
				resultString = null;
			} else {
				resultString = result.getString(1);
			}
		} catch (PSQLException e) {
			throw new AssertionError(queryString, e);
		}
		return resultString;
	}

	private List<CockroachDBExpression> mapped(CockroachDBAggregate aggregate) {
		switch (aggregate.getFunc()) {
		case SUM:
		case COUNT:
		case COUNT_ROWS:
		case BIT_AND:
		case BIT_OR:
		case XOR_AGG:
		case SUM_INT:
		case BOOL_AND:
		case BOOL_OR:
		case MAX:
		case MIN:
			return aliasArgs(Arrays.asList(aggregate));
		case AVG:
//			List<CockroachDBExpression> arg = Arrays.asList(new CockroachDBCast(aggregate.getExpr().get(0), CockroachDBDataType.DECIMAL.get()));
			CockroachDBAggregate sum = new CockroachDBAggregate(CockroachDBAggregateFunction.SUM, aggregate.getExpr());
			CockroachDBCast count = new CockroachDBCast(
					new CockroachDBAggregate(CockroachDBAggregateFunction.COUNT, aggregate.getExpr()),
					CockroachDBDataType.DECIMAL.get());
//			CockroachDBBinaryArithmeticOperation avg = new CockroachDBBinaryArithmeticOperation(sum, count, CockroachDBBinaryArithmeticOperator.DIV);
			return aliasArgs(Arrays.asList(sum, count));
		default:
			throw new AssertionError(aggregate.getFunc());
		}
	}

	private List<CockroachDBExpression> aliasArgs(List<CockroachDBExpression> originalAggregateArgs) {
		List<CockroachDBExpression> args = new ArrayList<>();
		int i = 0;
		for (CockroachDBExpression expr : originalAggregateArgs) {
			args.add(new CockroachDBAlias(expr, "agg" + i++));
		}
		return args;
	}

	private String getOuterAggregateFunction(CockroachDBAggregate aggregate) {
		switch (aggregate.getFunc()) {
		case AVG:
			return "SUM(agg0::DECIMAL)/SUM(agg1)::DECIMAL";
		case COUNT:
		case COUNT_ROWS:
			return CockroachDBAggregateFunction.SUM.toString() + "(agg0)";
		default:
			return aggregate.getFunc().toString() + "(agg0)";
		}
	}

	private CockroachDBSelect getSelect(List<CockroachDBExpression> aggregates, List<CockroachDBTableReference> from,
			CockroachDBExpression whereClause, List<CockroachDBJoin> joinList) {
		CockroachDBSelect leftSelect = new CockroachDBSelect();
		leftSelect.setColumns(aggregates);
		leftSelect.setFromTables(from);
		leftSelect.setWhereCondition(whereClause);
		leftSelect.setJoinList(joinList);
		if (Randomly.getBooleanWithSmallProbability()) {
			leftSelect.setGroupByClause(gen.generateExpressions(Randomly.smallNumber() + 1));
		}
		return leftSelect;
	}
	
	private void updateStateString() {
		StringBuilder sb = new StringBuilder();
		if (originalQuery != null) {
			sb.append(originalQuery);
			sb.append(";");
			if (firstResult != null) {
				sb.append("-- ");
				sb.append(firstResult);
			}
			sb.append("\n");
		}
		if (metamorphicQuery != null) {
			sb.append(metamorphicQuery);
			sb.append(";");
			if (secondResult != null) {
				sb.append("-- ");
				sb.append(secondResult);
			}
			sb.append("\n");
		}
	}

}
