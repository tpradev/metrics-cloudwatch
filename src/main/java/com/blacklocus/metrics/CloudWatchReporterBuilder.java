/**
 * Copyright 2013-2016 BlackLocus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blacklocus.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import java.util.SortedMap;


/**
 * A fluent style builder for a {@link CloudWatchReporter}. {@link #withNamespace(String)} is required. There
 * are suitable defaults for all other fields.
 */
public class CloudWatchReporterBuilder {

    // Defaults are resolved in build() so that 1) it is precisely clear what the caller set,
    // and 2) so that partial constructions can copy() before setting member variables with
    // things that should not be part of the copy such as a base builder for similar CloudWatchReporters.

    private MetricRegistry registry;
    private String namespace;
    private AmazonCloudWatchAsync client;
    private MetricFilter filter;
    private String dimensions;
    private Boolean timestampLocal;

    private Predicate<MetricDatum> reporterFilter;

    /**
     * @param registry of metrics for CloudWatchReporter to submit
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withRegistry(MetricRegistry registry) {
        this.registry = registry;
        return this;
    }

    /**
     * @param namespace metric namespace to use when submitting metrics to CloudWatch
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * @param client CloudWatch client
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withClient(AmazonCloudWatchAsync client) {
        this.client = client;
        return this;
    }

    /**
     * @param filter which returns true for metrics that should be sent to CloudWatch
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withFilter(MetricFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * @param dimensions global dimensions in the form name=value that should be appended to all metrics submitted to
     *                   CloudWatch
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withDimensions(String dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * @param timestampLocal whether or not to explicitly timestamp metric data to now (true), or leave it null so that
     *                       CloudWatch will timestamp it on receipt (false)
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withTimestampLocal(Boolean timestampLocal) {
        this.timestampLocal = timestampLocal;
        return this;
    }


    /**
     * This filter is applied right before submission to CloudWatch. This filter can access decoded metric name elements
     * such as {@link MetricDatum#getDimensions()}. true means to keep and submit the metric. false means to exclude it.
     * <p>
     * Different from {@link MetricFilter} in that
     * MetricFilter must operate on the encoded, single-string name (see {@link MetricFilter#matches(String, Metric)}),
     * and this filter is applied before {@link ScheduledReporter#report(SortedMap, SortedMap, SortedMap, SortedMap, SortedMap)} so that
     * filtered metrics never reach that method in CloudWatchReporter.
     * <p>
     * Defaults to {@link Predicates#alwaysTrue()} - i.e. do not remove any metrics from the submission due to this
     * particular filter.
     *
     * @param reporterFilter to replace 'alwaysTrue()'
     * @return this (for chaining)
     */
    public CloudWatchReporterBuilder withReporterFilter(Predicate<MetricDatum> reporterFilter) {
        this.reporterFilter = reporterFilter;
        return this;
    }

    /**
     * @return a new CloudWatchReporter instance based on the state of this builder
     */
    public CloudWatchReporter build() {
        Preconditions.checkState(!Strings.isNullOrEmpty(namespace), "Metric namespace is required.");

        // Use specified or fall back to default. Don't secretly modify the fields of this builder
        // in case the caller wants to re-use it to build other reporters, or something.

        MetricFilter resolvedFilter = null != filter ? filter : MetricFilter.ALL;
        String resolvedDimensions = null != dimensions ? dimensions : null;
        Boolean resolvedTimestampLocal = null != timestampLocal ? timestampLocal : false;

        Predicate<MetricDatum> resolvedReporterFilter = null != reporterFilter ? reporterFilter : Predicates.<MetricDatum>alwaysTrue();

        return new CloudWatchReporter(
                registry,
                namespace,
                resolvedFilter,
                client)
                .withDimensions(resolvedDimensions)
                .withReporterFilter(resolvedReporterFilter)
                .withTimestampLocal(resolvedTimestampLocal);
    }
}
