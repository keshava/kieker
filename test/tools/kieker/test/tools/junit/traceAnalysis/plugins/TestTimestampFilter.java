package kieker.test.tools.junit.traceAnalysis.plugins;

import java.util.concurrent.atomic.AtomicReference;
import junit.framework.TestCase;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.tools.traceAnalysis.plugins.executionFilter.TimestampFilter;
import kieker.test.tools.junit.traceAnalysis.util.ExecutionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Andre van Hoorn
 */
public class TestTimestampFilter extends TestCase {

    private static final Log log = LogFactory.getLog(TestTimestampFilter.class);
    private volatile long IGNORE_EXECUTIONS_BEFORE_TIMESTAMP = 50;
    private volatile long IGNORE_EXECUTIONS_AFTER_TIMESTAMP = 100;

    private final SystemModelRepository systemEntityFactory = new SystemModelRepository();
    private final ExecutionFactory eFactory = new ExecutionFactory(systemEntityFactory);

    /**
     * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
     * assert that a record <i>r</i> with <i>r.tin &lt; a</i> and <i>r.tout
     * &gt; a </i>, <i>r.tout &lt; b</i> does not pass the filter.
     */
    public void testRecordTinBeforeToutWithinIgnored() {
        TimestampFilter filter =
                new TimestampFilter(
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP);

        Execution exec = eFactory.genExecution(
                77, // traceId (value not important)
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP-1, // tin
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP-1,  // tout
                0, 0); // eoi, ess

        final AtomicReference<Boolean> filterPassedRecord =
                new AtomicReference<Boolean>(Boolean.FALSE);

        filter.getExecutionOutputPort().subscribe(new AbstractInputPort<Execution>("Execution input") {

            /**
             * In this test, this method should not be called.
             */
            public void newEvent(Execution event) {
                filterPassedRecord.set(Boolean.TRUE);
            }
        });
        filter.getExecutionInputPort().newEvent(exec);
        assertFalse("Filter passed execution " + exec
            + " although tin timestamp before" + IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
            filterPassedRecord.get());
    }

    /**
     * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
     * assert that a record <i>r</i> with <i>r.tin &gt; a</i>, <i>r.tin
     * &lt; b</i> and <i>r.tout &gt; b </i> does not pass the filter.
     */
    public void testRecordTinWithinToutAfterIgnored() {
        TimestampFilter filter =
                new TimestampFilter(
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP);

        Execution exec = eFactory.genExecution(
                15, // traceId (value not important)
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP+1, // tin
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP+1,  // tout
                0, 0); // eoi, ess

        final AtomicReference<Boolean> filterPassedRecord =
                new AtomicReference<Boolean>(Boolean.FALSE);

        filter.getExecutionOutputPort().subscribe(new AbstractInputPort<Execution>("Execution input") {

            /**
             * In this test, this method should not be called.
             */
            public void newEvent(Execution event) {
                filterPassedRecord.set(Boolean.TRUE);
            }
        });
        filter.getExecutionInputPort().newEvent(exec);
        assertFalse("Filter passed execution " + exec
            + " although tout timestamp after" + IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
            filterPassedRecord.get());
    }

    /**
     * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
     * assert that a record <i>r</i> with <i>r.tin == a</i> and <i>r.tout == b </i>
     * does pass the filter.
     */
    public void testRecordTinToutOnBordersPassed() {
        TimestampFilter filter =
                new TimestampFilter(
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP);

        final Execution exec = eFactory.genExecution(
                159, // traceId (value not important)
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP, // tin
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP,  // tout
                0, 0); // eoi, ess

        final AtomicReference<Boolean> filterPassedRecord =
                new AtomicReference<Boolean>(Boolean.FALSE);

        filter.getExecutionOutputPort().subscribe(new AbstractInputPort<Execution>("Execution input") {

            /**
             * In this test, this method MUST be called exactly once.
             */
            public void newEvent(Execution event) {
                filterPassedRecord.set(Boolean.TRUE);
                assertSame(event, exec);
            }
        });
        filter.getExecutionInputPort().newEvent(exec);
        assertTrue("Filter didn't pass execution " + exec
            + " although timestamps within range [" +
            IGNORE_EXECUTIONS_BEFORE_TIMESTAMP + "," +
            IGNORE_EXECUTIONS_AFTER_TIMESTAMP + "]",
            filterPassedRecord.get());
    }

    /**
     * Given a TimestampFilter selecting records within an interval <i>[a,b]</i>,
     * assert that a record <i>r</i> with <i>r.tin &gt; a</i>, <i>r.tin &lt; b</i>
     * and <i>r.tout &lt; b </i>, <i>r.tout &gt; a </i>  does pass the filter.
     */
    public void testRecordTinToutWithinRangePassed() {
        TimestampFilter filter =
                new TimestampFilter(
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP,
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP);

        final Execution exec = eFactory.genExecution(
                159, // traceId (value not important)
                IGNORE_EXECUTIONS_BEFORE_TIMESTAMP+1, // tin
                IGNORE_EXECUTIONS_AFTER_TIMESTAMP-1,  // tout
                0, 0); // eoi, ess

        final AtomicReference<Boolean> filterPassedRecord =
                new AtomicReference<Boolean>(Boolean.FALSE);

        filter.getExecutionOutputPort().subscribe(new AbstractInputPort<Execution>("Execution input") {

            /**
             * In this test, this method MUST be called exactly once.
             */
            public void newEvent(Execution event) {
                filterPassedRecord.set(Boolean.TRUE);
                assertSame(event, exec);
            }
        });
        filter.getExecutionInputPort().newEvent(exec);
        assertTrue("Filter didn't pass execution " + exec
            + " although timestamps within range [" +
            IGNORE_EXECUTIONS_BEFORE_TIMESTAMP + "," +
            IGNORE_EXECUTIONS_AFTER_TIMESTAMP + "]",
            filterPassedRecord.get());
    }
}