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
package kieker.analysis.trace;

import kieker.model.repository.SystemModelRepository;
import kieker.model.system.model.ExecutionTrace;

/**
 * TODO this class might be obsolete
 * This is the abstract base for a filter processing execution traces.
 *
 * @author Andre van Hoorn
 *
 * @since 1.1
 */
public abstract class AbstractExecutionTraceProcessingFilter extends AbstractTraceProcessingFilter<ExecutionTrace> {

	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param systemModelRepository
	 *            the used system model repository
	 */
	public AbstractExecutionTraceProcessingFilter(final SystemModelRepository systemModelRepository) {
		super(systemModelRepository);
	}

}
