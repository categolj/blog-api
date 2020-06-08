package am.ik.blog.config.r2dbc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import brave.Span;
import brave.Tracer;
import io.r2dbc.proxy.core.ConnectionInfo;
import io.r2dbc.proxy.core.ExecutionType;
import io.r2dbc.proxy.core.MethodExecutionInfo;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.core.QueryInfo;
import io.r2dbc.proxy.listener.LifeCycleListener;

import static java.util.stream.Collectors.joining;

/**
 * Listener to create spans for R2DBC SPI operations.
 *
 * @author Tadaya Tsuyukubo
 */
public class TracingExecutionListener implements LifeCycleListener {

	private static final String TAG_CONNECTION_ID = "connectionId";

	private static final String TAG_THREAD_ID = "threadId";

	private static final String TAG_THREAD_NAME = "threadName";

	private static final String TAG_QUERIES = "queries";

	private static final String TAG_BATCH_SIZE = "batchSize";

	private static final String TAG_QUERY_TYPE = "type";

	private static final String TAG_QUERY_SUCCESS = "success";

	private static final String TAG_QUERY_RESULT_COUNT = "resultCount";

	private static final String TAG_TRANSACTION_SAVEPOINT = "savepoint";

	private static final String TAG_TRANSACTION_COUNT = "transactionCount";

	private static final String TAG_COMMIT_COUNT = "commitCount";

	private static final String TAG_ROLLBACK_COUNT = "rollbackCount";

	private final Tracer tracer;

	private final String remoteServiceName;

	private Map<String, Span> connectionSpans = new ConcurrentHashMap<>();

	private Map<String, Span> transactionSpans = new ConcurrentHashMap<>();

	public TracingExecutionListener(Tracer tracer, String remoteServiceName) {
		this.tracer = tracer;
		this.remoteServiceName = remoteServiceName;
	}

	@Override
	public void beforeCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
		Span connectionSpan = this.tracer.nextSpan().kind(Span.Kind.CLIENT).remoteServiceName(remoteServiceName).start();

		// store the span for retrieval at "afterCreateOnConnectionFactory"
		methodExecutionInfo.getValueStore().put("connectionSpan", connectionSpan);
	}

	@Override
	public void afterCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
		// retrieve the span created at "beforeCreateOnConnectionFactory"
		Span connectionSpan = methodExecutionInfo.getValueStore().get("connectionSpan", Span.class);

		Throwable thrown = methodExecutionInfo.getThrown();
		if (thrown != null) {
			connectionSpan.error(thrown).finish();
			return;
		}

		String connectionId = methodExecutionInfo.getConnectionInfo().getConnectionId();

		connectionSpan.name("r2dbc:connection").tag(TAG_CONNECTION_ID, connectionId)
				.tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
				.tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName()).annotate("Connection Acquired");

		this.connectionSpans.put(connectionId, connectionSpan);
	}

	@Override
	public void afterCloseOnConnection(MethodExecutionInfo methodExecutionInfo) {
		ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
		String connectionId = connectionInfo.getConnectionId();
		Span connectionSpan = this.connectionSpans.remove(connectionId);
		if (connectionSpan == null) {
			return; // already closed
		}
		Throwable thrown = methodExecutionInfo.getThrown();
		if (thrown != null) {
			connectionSpan.error(thrown);
		}
		connectionSpan.tag(TAG_CONNECTION_ID, connectionId)
				.tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
				.tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName())
				.tag(TAG_TRANSACTION_COUNT, String.valueOf(connectionInfo.getTransactionCount()))
				.tag(TAG_COMMIT_COUNT, String.valueOf(connectionInfo.getCommitCount()))
				.tag(TAG_ROLLBACK_COUNT, String.valueOf(connectionInfo.getRollbackCount())).finish();
	}

	@Override
	public void beforeExecuteOnBatch(QueryExecutionInfo queryExecutionInfo) {
		beforeExecuteQuery(queryExecutionInfo);
	}

	@Override
	public void beforeExecuteOnStatement(QueryExecutionInfo queryExecutionInfo) {
		beforeExecuteQuery(queryExecutionInfo);
	}

	private void beforeExecuteQuery(QueryExecutionInfo queryExecutionInfo) {
		String connectionId = queryExecutionInfo.getConnectionInfo().getConnectionId();

		String queries = queryExecutionInfo.getQueries().stream().map(QueryInfo::getQuery).collect(joining(", "));

		Span querySpan = this.tracer.nextSpan().name("r2dbc:query").kind(Span.Kind.CLIENT)
				.tag(TAG_CONNECTION_ID, connectionId).tag(TAG_QUERY_TYPE, queryExecutionInfo.getType().toString())
				.tag(TAG_QUERIES, queries).start();

		if (ExecutionType.BATCH == queryExecutionInfo.getType()) {
			querySpan.tag(TAG_BATCH_SIZE, Integer.toString(queryExecutionInfo.getBatchSize()));
		}

		queryExecutionInfo.getValueStore().put("querySpan", querySpan);
	}

	@Override
	public void afterExecuteOnBatch(QueryExecutionInfo queryExecutionInfo) {
		afterExecuteQuery(queryExecutionInfo);
	}

	@Override
	public void afterExecuteOnStatement(QueryExecutionInfo queryExecutionInfo) {
		afterExecuteQuery(queryExecutionInfo);
	}

	private void afterExecuteQuery(QueryExecutionInfo queryExecutionInfo) {
		Span querySpan = queryExecutionInfo.getValueStore().get("querySpan", Span.class);

		querySpan.tag(TAG_THREAD_ID, String.valueOf(queryExecutionInfo.getThreadId()))
				.tag(TAG_THREAD_NAME, queryExecutionInfo.getThreadName())
				.tag(TAG_QUERY_SUCCESS, Boolean.toString(queryExecutionInfo.isSuccess()));

		Throwable thrown = queryExecutionInfo.getThrowable();
		if (thrown != null) {
			querySpan.error(thrown);
		}
		else {
			// TODO: impl result count if possible
			// querySpan.tag(TAG_QUERY_RESULT_COUNT,
			// Integer.toString(queryExecutionInfo.getCurrentResultCount()));
		}
		querySpan.finish();
	}

	@Override
	public void beforeBeginTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
		String connectionId = methodExecutionInfo.getConnectionInfo().getConnectionId();
		Span transactionSpan = this.tracer.nextSpan().name("r2dbc:transaction").start();

		this.transactionSpans.put(connectionId, transactionSpan);
	}

	@Override
	public void afterCommitTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
		String connectionId = methodExecutionInfo.getConnectionInfo().getConnectionId();

		Span transactionSpan = this.transactionSpans.remove(connectionId);
		if (transactionSpan != null) {
			transactionSpan.annotate("commit").tag(TAG_CONNECTION_ID, connectionId)
					.tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
					.tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName()).finish();
		}

		Span connectionSpan = this.connectionSpans.get(connectionId);
		if (connectionSpan != null) {
			connectionSpan.annotate("Transaction commit");
		}
	}

	@Override
	public void afterRollbackTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
		String connectionId = methodExecutionInfo.getConnectionInfo().getConnectionId();

		Span transactionSpan = this.transactionSpans.remove(connectionId);
		if (transactionSpan != null) {
			transactionSpan.annotate("rollback").tag(TAG_CONNECTION_ID, connectionId)
					.tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
					.tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName()).finish();
		}

		Span connectionSpan = this.connectionSpans.get(connectionId);
		if (connectionSpan != null) {
			connectionSpan.annotate("Transaction rollback");
		}
	}

	@Override
	public void afterRollbackTransactionToSavepointOnConnection(MethodExecutionInfo methodExecutionInfo) {
		String connectionId = methodExecutionInfo.getConnectionInfo().getConnectionId();
		String savepoint = (String) methodExecutionInfo.getMethodArgs()[0];

		Span transactionSpan = this.transactionSpans.remove(connectionId);
		if (transactionSpan != null) {
			transactionSpan.annotate("rollback to savepoint").tag(TAG_TRANSACTION_SAVEPOINT, savepoint)
					.tag(TAG_CONNECTION_ID, connectionId)
					.tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
					.tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName()).finish();
		}

		Span connectionSpan = this.connectionSpans.get(connectionId);
		if (connectionSpan != null) {
			connectionSpan.annotate("Transaction rollback to savepoint");
		}
	}
}