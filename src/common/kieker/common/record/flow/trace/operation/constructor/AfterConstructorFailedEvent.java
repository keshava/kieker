/***************************************************************************
 * Copyright 2013 Kieker Project (http://kieker-monitoring.net)
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

package kieker.common.record.flow.trace.operation.constructor;

import kieker.common.record.flow.IConstructorRecord;
import kieker.common.record.flow.trace.operation.AfterOperationFailedEvent;

/**
 * @author Jan Waller
 * 
 * @since 1.6
 */
public class AfterConstructorFailedEvent extends AfterOperationFailedEvent implements IConstructorRecord {
	private static final long serialVersionUID = 148777586227402929L;
	public static final Class<?>[] TYPES = AfterOperationFailedEvent.TYPES;

	/**
	 * This constructor initializes the fields of the record using the given parameters.
	 * 
	 * @param timestamp
	 *            The timestamp.
	 * @param traceId
	 *            The trace ID.
	 * @param orderIndex
	 *            The order index.
	 * @param operationSignature
	 *            The operation signature. This parameter can be null.
	 * @param classSignature
	 *            The class signature. This parameter can be null.
	 * @param cause
	 *            The cause. This parameter can be null.
	 */
	public AfterConstructorFailedEvent(final long timestamp, final long traceId, final int orderIndex, final String operationSignature, final String classSignature,
			final String cause) {
		super(timestamp, traceId, orderIndex, operationSignature, classSignature, cause);
	}

	/**
	 * This constructor converts the given array into a record. It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public AfterConstructorFailedEvent(final Object[] values) { // NOPMD (values stored directly)
		super(values, TYPES); // values[0..5]
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param types
	 *            The types of the elements in the first array.
	 */
	protected AfterConstructorFailedEvent(final Object[] values, final Class<?>[] types) { // NOPMD (values stored directly)
		super(values, types); // values[0..4]
	}
}
