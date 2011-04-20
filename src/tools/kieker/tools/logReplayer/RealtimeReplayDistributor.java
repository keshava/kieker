package kieker.tools.logReplayer;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2010 Kieker Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 *
 */

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kieker.analysis.plugin.IMonitoringRecordConsumerPlugin;
import kieker.analysis.plugin.MonitoringRecordConsumerException;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.timer.DefaultSystemTimer;
import kieker.monitoring.timer.ITimeSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IMonitoringRecordConsumerPlugin that distributes the log records to the worker
 * thread for "real time" replays.
 *
 * @author Robert von Massow
 *
 */
public class RealtimeReplayDistributor implements IMonitoringRecordConsumerPlugin {

    private static final Log log = LogFactory.getLog(RealtimeReplayDistributor.class);
    public final int numWorkers;
    private final IMonitoringRecordConsumerPlugin cons;
    private volatile long startTime = -1, offset = -1, firstLoggingTimestamp;
    private final ScheduledThreadPoolExecutor executor;
    private long lTime;
    private volatile int active;
    private final int maxQueueSize;
    private final CountDownLatch terminationLatch;
    private final static ITimeSource timesource = DefaultSystemTimer.getInstance();

    /** Private constructor must not be used */
    @SuppressWarnings("unused")
	private RealtimeReplayDistributor() {
        this.executor = null;
        this.numWorkers = -1;
        this.cons = null;
        this.maxQueueSize = -1;
        this.terminationLatch = null;
    }

    /**
     * Constructs a RealtimeReplayDistributor.
     * 
     * @param numWorkers number of worker threads processing the internal record buffer
     * @param cons the consumer
     * @param terminationLatch will be decremented after the last record was replayed
     */
    public RealtimeReplayDistributor(final int numWorkers, final IMonitoringRecordConsumerPlugin cons, final CountDownLatch terminationLatch) {
        this.numWorkers = numWorkers;
        this.cons = cons;
        this.maxQueueSize = numWorkers * 1000;
        this.executor = new ScheduledThreadPoolExecutor(numWorkers);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        this.terminationLatch = terminationLatch;
    }

    //private static final String outputFn = "SchedulingList";
    //private static PrintStream ps;
    @Override
	public boolean newMonitoringRecord(
            final IMonitoringRecord monitoringRecord) {
        if (this.startTime == -1) { // init on first record
            //try {
            // init on first record
            //ps = new PrintStream(new FileOutputStream(outputFn, false)); // do not append
            //} catch (FileNotFoundException ex) {
            //    log.info("FileNotFound:", ex);
            //}
            this.firstLoggingTimestamp = monitoringRecord.getLoggingTimestamp() - (1 * 1000 * 1000); // 1 millisecond tolerance
            this.offset = (2 * 1000 * 1000 * 1000) - this.firstLoggingTimestamp;
            this.startTime = timesource.getTime();
            //log.info("firstLoggingTimeStamp: " + this.firstLoggingTimestamp);
            //log.info("offset: " + this.offset);
            //log.info("startTime" + this.startTime);
        }
        if (monitoringRecord.getLoggingTimestamp() < this.firstLoggingTimestamp) {
            final MonitoringRecordConsumerException e = new MonitoringRecordConsumerException("Timestamp of current record " + monitoringRecord.getLoggingTimestamp() + " < firstLoggingTimestamp " + this.firstLoggingTimestamp);
            RealtimeReplayDistributor.log.error("RecordConsumerExecutionException", e);
            return false;
        }
        final long schedTime = (monitoringRecord.getLoggingTimestamp() + this.offset) // relative to 1st record
                - (timesource.getTime() - this.startTime); // substract elapsed time
        //ps.println("curT.record: " + monitoringRecord.getLoggingTimestamp());ps.flush();
        //ps.println("curT.ctrl: " + ctrlnst.currentTimeNanos());ps.flush();
        //ps.println("elapsedT (nsec): " + (ctrlnst.currentTimeNanos() - this.startTime));
        //ps.println("schedTime (nsec): " + schedTime);
        if (schedTime < 0) {
            final MonitoringRecordConsumerException e = new MonitoringRecordConsumerException("negative scheduling time: " + schedTime);
            RealtimeReplayDistributor.log.error("RecordConsumerExecutionException", e);
            return false;
        }
        synchronized (this) {
            if (this.active > this.maxQueueSize) {
                //log.info("this.active > this.maxQueueSize ("+this.active+"<"+this.maxQueueSize+")");
                try {
                    this.wait();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.active++;
            this.executor.schedule(new RealtimeReplayWorker(monitoringRecord, this, this.cons),
                    schedTime, TimeUnit.NANOSECONDS); // *relative* delay from now

        }
        this.lTime = this.lTime < monitoringRecord.getLoggingTimestamp() ? monitoringRecord.getLoggingTimestamp()
                : this.lTime;
        return true;
    }

    @Override
	public boolean execute() {
        return true;
    }

    @Override
	public Collection<Class<? extends IMonitoringRecord>> getRecordTypeSubscriptionList() {
        return null;
    }

    public final long getOffset() {
        return this.offset;
    }

    public final long getStartTime() {
        return this.startTime;
    }

    @Override
	public void terminate(final boolean error) {
        final long terminationDelay = (this.lTime + this.offset) - (timesource.getTime() - this.startTime) + 100000000;
        RealtimeReplayDistributor.log.info("Will terminate in " + terminationDelay + "nsecs from now");
        this.executor.schedule(new Runnable() {

            @Override
			public void run() {
                //ctrlnst.terminateMonitoring();
                if (RealtimeReplayDistributor.this.terminationLatch != null) {
                    RealtimeReplayDistributor.this.terminationLatch.countDown(); // signal that last record has been scheduled
                } else {
                    RealtimeReplayDistributor.log.warn("terminationLatch == null");
                }
                //cons.terminate(error);
                //log.info("Terminating Controller");
            }
        }, terminationDelay, TimeUnit.NANOSECONDS);
        this.executor.shutdown();
    }

    public synchronized void decreaseActive() {
        this.active--;
        this.notify();
    }
}
