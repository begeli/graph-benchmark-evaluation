package ch.ethz.spcl.query;

public class QueryResultSummary {
    private int queryId;
    private long executionTime;
    private int failedQueryCount;

    public QueryResultSummary(int queryId, long executionTime, int failedQueryCount) {
        this.queryId = queryId;
        this.executionTime = executionTime;
        this.failedQueryCount = failedQueryCount;
    }

    public int getQueryId() {
        return queryId;
    }
    public long getExecutionTime() {
        return executionTime;
    }
    public int getFailedQueryCount() {
        return failedQueryCount;
    }
}
