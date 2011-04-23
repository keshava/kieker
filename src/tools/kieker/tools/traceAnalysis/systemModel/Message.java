package kieker.tools.traceAnalysis.systemModel;

/**
 *
 * @author Andre van Hoorn
 */
public abstract class Message {

    private final long timestamp;
    private final Execution sendingExecution, receivingExecution;

    protected Message() {
        this.timestamp = -1;
        this.sendingExecution = null;
        this.receivingExecution = null;
    }

    public Message(final long timestamp,
            final Execution sendingExecution,
            final Execution receivingExecution) {
        this.timestamp = timestamp;
        this.sendingExecution = sendingExecution;
        this.receivingExecution = receivingExecution;
    }

    public final Execution getReceivingExecution() {
        return this.receivingExecution;
    }

    public final Execution getSendingExecution() {
        return this.sendingExecution;
    }

    public final long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        StringBuilder strBuild = new StringBuilder();

        if (this instanceof SynchronousCallMessage) {
            strBuild.append("SYNC-CALL").append(" ");
        } else {
            strBuild.append("SYNC-RPLY").append(" ");
        }

        strBuild.append(this.timestamp);
        strBuild.append(" ");
        if (this.getSendingExecution().getOperation().getId() == Operation.ROOT_OPERATION_ID) {
            strBuild.append("$");
        } else {
            strBuild.append(this.getSendingExecution());
        }
        strBuild.append(" --> ");
        if (this.getReceivingExecution().getOperation().getId() == Operation.ROOT_OPERATION_ID) {
            strBuild.append("$");
        } else {
            strBuild.append(this.getReceivingExecution());
        }
        return strBuild.toString();
    }

    @Override
    public abstract boolean equals(Object obj);
}
