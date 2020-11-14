/***************************************************************************
 * Copyright 2020 Kieker Project (http://kieker-monitoring.net)
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
package kieker.analysis.plugin.reader.depcompression;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.tukaani.xz.XZInputStream;

import kieker.common.configuration.Configuration;
import kieker.common.util.filesystem.FSUtil;

/**
 * Decompression filter for stream reading stages providing XZ-decompression.
 *
 * @author Reiner Jung
 *
 * @since 1.15
 *
 */
public class XZDecompressionFilter extends AbstractDecompressionFilter {

	public static final String PREFIX = XZDecompressionFilter.class.getCanonicalName();

	public static final String BUFFER_SIZE = PREFIX + "bufferSize";
	public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

	private final int bufferSize;

	/**
	 * Create a new XZ decompression filter.
	 *
	 * @param configuration
	 *            configuration settings
	 */
	public XZDecompressionFilter(final Configuration configuration) {
		super(configuration);
		this.bufferSize = configuration.getIntProperty(BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kieker.analysis.plugin.reader.depcompression.IDecompressionFilter#chainInputStream(java.io.OutputStream)
	 */
	@Override
	public InputStream chainInputStream(final InputStream inputStream) throws IOException {
		return new BufferedInputStream(new XZInputStream(inputStream, this.bufferSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kieker.analysis.plugin.reader.depcompression.IDecompressionFilter#getExtension()
	 */
	@Override
	public String getExtension() {
		return FSUtil.XZ_FILE_EXTENSION;
	}

}
