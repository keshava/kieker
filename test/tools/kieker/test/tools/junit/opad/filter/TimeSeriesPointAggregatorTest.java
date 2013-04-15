/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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

package kieker.test.tools.junit.opad.filter;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.list.ListReader;
import kieker.common.configuration.Configuration;
import kieker.tools.opad.filter.TimeSeriesPointAggregator;
import kieker.tools.opad.record.NamedDoubleTimeSeriesPoint;

/**
 * 
 * @author Tom Frotscher
 * 
 */
public class TimeSeriesPointAggregatorTest {

	private AnalysisController controller;

	// Variables ForecastingFilter
	private static final String OP_SIGNATURE_A = "a.A.opA";
	private ListReader<NamedDoubleTimeSeriesPoint> theReaderAggregator;
	private TimeSeriesPointAggregator aggregator;
	private ListCollectionFilter<NamedDoubleTimeSeriesPoint> sinkPlugin;

	// HelperMethods ForecastingFilter

	private NamedDoubleTimeSeriesPoint createNDTSP(final String signature, final double value) {
		final NamedDoubleTimeSeriesPoint r = new NamedDoubleTimeSeriesPoint(new Date(), value, signature);
		return r;
	}

	// Test for the Aggregator Filter
	@Test
	public void testAggregatorOnly() throws InterruptedException, IllegalStateException, AnalysisConfigurationException {

		this.controller = new AnalysisController();

		// READER
		final Configuration readerAggregationConfiguration = new Configuration();
		this.theReaderAggregator = new ListReader<NamedDoubleTimeSeriesPoint>(readerAggregationConfiguration, this.controller);
		int i = 1;
		while (i < 9000) {
			this.theReaderAggregator.addObject(this.createNDTSP(OP_SIGNATURE_A, i));
			i++;
		}

		// AGGREGATIONFILTER
		final Configuration aggregationConfiguration = new Configuration();
		aggregationConfiguration.setProperty(TimeSeriesPointAggregator.CONFIG_PROPERTY_NAME_AGGREGATION_SPAN, "20");
		aggregationConfiguration.setProperty(TimeSeriesPointAggregator.CONFIG_PROPERTY_NAME_AGGREGATION_TIMEUNIT, "MILLISECONDS");
		// aggregationConfiguration.setProperty(TimeSeriesPointAggregator.CONFIG_PROPERTY_NAME_AGGREGATION_METHOD, "MAX");
		this.aggregator = new TimeSeriesPointAggregator(aggregationConfiguration, this.controller);

		// SINK 1
		this.sinkPlugin = new ListCollectionFilter<NamedDoubleTimeSeriesPoint>(new Configuration(), this.controller);
		Assert.assertTrue(this.sinkPlugin.getList().isEmpty());

		// CONNECTION
		this.controller.connect(this.theReaderAggregator, ListReader.OUTPUT_PORT_NAME, this.aggregator, TimeSeriesPointAggregator.INPUT_PORT_NAME_TSPOINT);
		this.controller.connect(this.aggregator,
				TimeSeriesPointAggregator.OUTPUT_PORT_NAME_AGGREGATED_TSPOINT, this.sinkPlugin,
				ListCollectionFilter.INPUT_PORT_NAME);
		Assert.assertEquals(0, this.sinkPlugin.getList().size());
		this.controller.run();
		System.out.println("outputs: " + this.sinkPlugin.getList().size());
		int j = 0;
		while (j <= (this.sinkPlugin.getList().size() - 1)) {
			System.out
					.println("value: " + this.sinkPlugin.getList().get(j).getDoubleValue() + " timestamp: " + this.sinkPlugin.getList().get(j).getTime().getTime()
							+ " name: " + this.sinkPlugin.getList().get(j).getName());
			j++;
		}

	}
}