/***************************************************************************
 * Copyright 2017 Kieker Project (http://kieker-monitoring.net)
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
 ***************************************************************************/

package kieker.monitoring.writer.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.DefaultValueSerializer;
import kieker.common.record.io.IValueSerializer;
import kieker.common.record.misc.RegistryRecord;
import kieker.monitoring.registry.GetIdAdapter;
import kieker.monitoring.registry.IRegistryListener;
import kieker.monitoring.registry.WriterRegistry;
import kieker.monitoring.writer.AbstractMonitoringWriter;
import kieker.monitoring.writer.WriterUtil;

/**
 * Represents a monitoring writer which serializes records via TCP to a given
 * host:port.
 * 
 * @author Christian Wulf
 *
 * @since 1.13
 */
public class SingleSocketTcpWriter extends AbstractMonitoringWriter implements IRegistryListener<String> {

	/** the logger for this class. */
	private static final Log LOG = LogFactory.getLog(SingleSocketTcpWriter.class);
	/**
	 * This writer can be configured by the configuration file "kieker.properties".
	 * For this purpose, it uses this prefix for all configuration keys.
	 */
	private static final String PREFIX = SingleSocketTcpWriter.class.getName() + ".";
	/**
	 * when the initial connection to a reader fails, this value indicate how long
	 * the writer should wait for another connection attempt:
	 */
	private static final long WAITING_TIME_TO_RECONNECT_IN_MS = 100;

	/** configuration key for the hostname. */
	public static final String CONFIG_HOSTNAME = PREFIX + "hostname"; // NOCS
																		// (afterPREFIX)
	/** configuration key for the port. */
	public static final String CONFIG_PORT = PREFIX + "port"; // NOCS
																// (afterPREFIX)
	/** configuration key for the size of the {@link #buffer}. */
	public static final String CONFIG_BUFFERSIZE = PREFIX + "bufferSize"; // NOCS
																			// (afterPREFIX)
	/** configuration key for {@link #flush}. */
	public static final String CONFIG_FLUSH = PREFIX + "flush"; // NOCS

	/** configuration key for {@link #connectionTimeoutInMs}. */
	public static final String CONFIG_CONN_TIMEOUT_IN_MS = PREFIX + "connectionTimeoutInMs";

	// (afterPREFIX)
	/** the host name and the port of the record reader */
	private final InetSocketAddress socketAddress;
	/** the connection timeout for the initial connect */
	private final long connectionTimeoutInMs;
	/** the channel which writes out monitoring and registry records. */
	private final SocketChannel socketChannel;
	/** the buffer used for buffering monitoring records. */
	private final ByteBuffer buffer;
	/** the buffer used for buffering registry records. */
	private final ByteBuffer registryBuffer;
	/**
	 * <code>true</code> if the {@link #buffer} should be flushed upon each new
	 * incoming monitoring record.
	 */
	private final boolean flush;
	/** the serializer to use for the incoming records */
	private final IValueSerializer serializer;

	// remove RegisterAdapter

	public SingleSocketTcpWriter(final Configuration configuration) throws IOException {
		super(configuration);
		final String hostname = configuration.getStringProperty(CONFIG_HOSTNAME);
		final int port = configuration.getIntProperty(CONFIG_PORT);
		this.socketAddress = new InetSocketAddress(hostname, port);
		this.connectionTimeoutInMs = configuration.getLongProperty(CONFIG_CONN_TIMEOUT_IN_MS, 0);
		this.socketChannel = SocketChannel.open();
		// buffer size is available by byteBuffer.capacity()
		// TODO should we check for buffers too small for a single record?
		final int bufferSize = this.configuration.getIntProperty(CONFIG_BUFFERSIZE);
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.registryBuffer = ByteBuffer.allocateDirect(bufferSize);
		this.flush = configuration.getBooleanProperty(CONFIG_FLUSH);

		final WriterRegistry writerRegistry = new WriterRegistry(this);
		this.serializer = DefaultValueSerializer.create(this.buffer, new GetIdAdapter<>(writerRegistry));
	}

	@Override
	public void onStarting() {
		try {
			this.socketChannel.configureBlocking(false);
			this.socketChannel.connect(this.socketAddress);

			long startTimeInNs = System.nanoTime();
			while (!this.socketChannel.finishConnect()) {
				tryReconnect(startTimeInNs);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void tryReconnect(long startTimeInNs) {
		final long currentTimeInNs = System.nanoTime();
		final long diffInMs = TimeUnit.NANOSECONDS.toMillis(currentTimeInNs - startTimeInNs);

		if (diffInMs > this.connectionTimeoutInMs) {
			String message = String.format("Connection timeout of %d ms exceeded.", this.connectionTimeoutInMs);
			throw new IllegalStateException(message);
		}

		try {
			Thread.sleep(WAITING_TIME_TO_RECONNECT_IN_MS);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void writeMonitoringRecord(final IMonitoringRecord monitoringRecord) {
		final ByteBuffer recordBuffer = this.buffer;
		if ((4 + 8 + monitoringRecord.getSize()) > recordBuffer.remaining()) {
			// Always flush the registryBuffer before flushing the recordBuffer.
			// Otherwise the monitoring records could arrive before their string
			// records
			WriterUtil.flushBuffer(this.registryBuffer, this.socketChannel, LOG);
			WriterUtil.flushBuffer(recordBuffer, this.socketChannel, LOG);
		}

		final String recordClassName = monitoringRecord.getClass().getName();

		this.serializer.putString(recordClassName);
		this.serializer.putLong(monitoringRecord.getLoggingTimestamp());
		monitoringRecord.serialize(this.serializer);

		if (this.flush) {
			// Always flush the registryBuffer before flushing the recordBuffer.
			// Otherwise the monitoring records could arrive before their string
			// records
			WriterUtil.flushBuffer(this.registryBuffer, this.socketChannel, LOG);
			WriterUtil.flushBuffer(recordBuffer, this.socketChannel, LOG);
		}
	}

	@Override
	public void onNewRegistryEntry(final String value, final int id) {
		final ByteBuffer localRegistryBuffer = this.registryBuffer;

		final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		// logging timestamp + class id + RegistryRecord.SIZE + bytes.length
		final int requiredBufferSize = (2 * AbstractMonitoringRecord.TYPE_SIZE_INT) + RegistryRecord.SIZE
				+ bytes.length;

		if (localRegistryBuffer.remaining() < requiredBufferSize) {
			WriterUtil.flushBuffer(localRegistryBuffer, this.socketChannel, LOG);
		}

		localRegistryBuffer.putInt(RegistryRecord.CLASS_ID);
		localRegistryBuffer.putInt(id);
		localRegistryBuffer.putInt(value.length());
		localRegistryBuffer.put(bytes);
	}

	@Override
	public void onTerminating() {
		// Always flush the registryBuffer before flushing the recordBuffer.
		// Otherwise the monitoring records could arrive before their string
		// records
		WriterUtil.flushBuffer(this.registryBuffer, this.socketChannel, LOG);
		WriterUtil.flushBuffer(this.buffer, this.socketChannel, LOG);
		WriterUtil.close(this.socketChannel, LOG);
	}
}
