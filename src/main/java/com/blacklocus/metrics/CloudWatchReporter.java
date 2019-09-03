/**
 * Copyright 2013-2016 BlackLocus
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blacklocus.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.StatisticSet;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.blacklocus.metrics.Constants.*;

/**
 * New users should obtain a reporter via a {@link CloudWatchReporterBuilder}! The reporter constructors remain
 * for legacy users of this package.
 * <p>
 * Please refer to [README.md](https://github.com/blacklocus/metrics-cloudwatch/blob/master/README.md) for the
 * latest usage documentation.
 *
 * @author Jason Dunkelberger (dirkraft)
 */
public class CloudWatchReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchReporter.class);


    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#NAME_TOKEN_DELIMITER_RGX}
     */
    @Deprecated
    public static final String NAME_TOKEN_DELIMITER_RGX = Constants.NAME_TOKEN_DELIMITER_RGX;

    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#NAME_TOKEN_DELIMITER}
     */
    @Deprecated
    public static final String NAME_TOKEN_DELIMITER = Constants.NAME_TOKEN_DELIMITER;

    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#NAME_DIMENSION_SEPARATOR}
     */
    @Deprecated
    public static final String NAME_DIMENSION_SEPARATOR = Constants.NAME_DIMENSION_SEPARATOR;

    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#NAME_PERMUTE_MARKER}
     */
    @Deprecated
    public static final String NAME_PERMUTE_MARKER = Constants.NAME_PERMUTE_MARKER;

    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#VALID_NAME_TOKEN_RGX}
     */
    @Deprecated
    public static final String VALID_NAME_TOKEN_RGX = Constants.VALID_NAME_TOKEN_RGX;

    /**
     * @deprecated maintained for backwards compatibility. Moved to {@link Constants#VALID_DIMENSION_PART_RGX}
     */
    @Deprecated
    public static final String VALID_DIMENSION_PART_RGX = Constants.VALID_DIMENSION_PART_RGX;



    @Deprecated
    static final MetricFilter ALL = MetricFilter.ALL;


    /**
     * Submit metrics to CloudWatch under this metric namespace
     */
    private final String metricNamespace;

    private final AmazonCloudWatchAsync cloudWatch;

    /**
     * We only submit the difference in counters since the last submission. This way we don't have to reset the counters
     * within this application.
     */
    private final Map<Counting, Long> lastPolledCounts = new HashMap<Counting, Long>();


    /**
     * Optional, global reporter-wide dimensions automatically appended to all metrics.
     */
    private String dimensions;

    /**
     * Whether or not to explicitly timestamp metric data to local now (true), or leave it null so that
     * CloudWatch will timestamp it on receipt (false). Defaults to false.
     */
    private boolean timestampLocal = false;

    /**
     * This filter is applied right before submission to CloudWatch. This filter can access decoded metric name elements
     * such as {@link MetricDatum#getDimensions()}.
     * <p>
     * Different from {@link MetricFilter} in that
     * MetricFilter must operate on the encoded, single-string name (see {@link MetricFilter#matches(String, Metric)}),
     * and this filter is applied before {@link #report(SortedMap, SortedMap, SortedMap, SortedMap, SortedMap)} so that
     * filtered metrics never reach that method in this reporter.
     * <p>
     * Defaults to {@link Predicates#alwaysTrue()} - i.e. do not remove any metrics from the submission due to this
     * particular filter.
     */
    private Predicate<MetricDatum> reporterFilter = Predicates.alwaysTrue();

    /**
     * Creates a new {@link ScheduledReporter} instance. The reporter does not report metrics until
     * {@link #start(long, TimeUnit)}.
     *
     * @param registry   the {@link MetricRegistry} containing the metrics this reporter will report
     * @param cloudWatch client
     */
    public CloudWatchReporter(MetricRegistry registry, AmazonCloudWatchAsync cloudWatch) {
        this(registry, null, cloudWatch);
    }

    /**
     * Creates a new {@link ScheduledReporter} instance. The reporter does not report metrics until
     * {@link #start(long, TimeUnit)}.
     *
     * @param registry        the {@link MetricRegistry} containing the metrics this reporter will report
     * @param metricNamespace (optional) CloudWatch metric namespace that all metrics reported by this reporter will
     *                        fall under
     * @param cloudWatch      client
     */
    public CloudWatchReporter(MetricRegistry registry,
                              String metricNamespace,
                              AmazonCloudWatchAsync cloudWatch) {
        this(registry, metricNamespace, MetricFilter.ALL, cloudWatch);
    }

    /**
     * Creates a new {@link ScheduledReporter} instance. The reporter does not report metrics until
     * {@link #start(long, TimeUnit)}.
     *
     * @param registry        the {@link MetricRegistry} containing the metrics this reporter will report
     * @param metricNamespace (optional) CloudWatch metric namespace that all metrics reported by this reporter will
     *                        fall under
     * @param metricFilter    (optional) see {@link MetricFilter}
     * @param cloudWatch      client
     */
    public CloudWatchReporter(MetricRegistry registry,
                              String metricNamespace,
                              MetricFilter metricFilter,
                              AmazonCloudWatchAsync cloudWatch) {

        super(registry, "CloudWatchReporter:" + metricNamespace, metricFilter, TimeUnit.MINUTES, TimeUnit.MINUTES);

        this.metricNamespace = metricNamespace;
        this.cloudWatch = cloudWatch;
    }

    /**
     * Sets global reporter-wide dimensions and returns itself.
     *
     * @param dimensions (optional) the string representing global dimensions
     * @return this (for chaining)
     */
    public CloudWatchReporter withDimensions(String dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * @param timestampLocal Whether or not to explicitly timestamp metric data to now (true), or leave it null so that
     *                       CloudWatch will timestamp it on receipt (false). Defaults to false.
     * @return this (for chaining)
     */
    public CloudWatchReporter withTimestampLocal(boolean timestampLocal) {
        this.timestampLocal = timestampLocal;
        return this;
    }


    /**
     * This filter is applied right before submission to CloudWatch. This filter can access decoded metric name elements
     * such as {@link MetricDatum#getDimensions()}.
     * <p>
     * Different from {@link MetricFilter} in that
     * MetricFilter must operate on the encoded, single-string name (see {@link MetricFilter#matches(String, Metric)}),
     * and this filter is applied before {@link #report(SortedMap, SortedMap, SortedMap, SortedMap, SortedMap)} so that
     * filtered metrics never reach that method in this reporter.
     * <p>
     * Defaults to {@link Predicates#alwaysTrue()} - i.e. do not remove any metrics from the submission due to this
     * particular filter.
     *
     * @param reporterFilter to replace 'alwaysTrue()'
     * @return this (for chaining)
     */
    public CloudWatchReporter withReporterFilter(Predicate<MetricDatum> reporterFilter) {
        this.reporterFilter = reporterFilter;
        return this;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        try {
            // Just an estimate to reduce resizing.
            List<MetricDatum> data = new ArrayList<MetricDatum>(
                    gauges.size() + counters.size() + meters.size() + 2 * histograms.size() + 2 * timers.size()
            );

            // Translate various metric classes to MetricDatum
            for (Map.Entry<String, Gauge> gaugeEntry : gauges.entrySet()) {
                reportGauge(gaugeEntry, data);
            }
            for (Map.Entry<String, Counter> counterEntry : counters.entrySet()) {
                reportCounter(counterEntry, data);
            }
            for (Map.Entry<String, Meter> meterEntry : meters.entrySet()) {
                reportCounter(meterEntry, data);
            }
            for (Map.Entry<String, Histogram> histogramEntry : histograms.entrySet()) {
                reportCounter(histogramEntry, data);
                reportSampling(histogramEntry, 1.0, data);
            }
            for (Map.Entry<String, Timer> timerEntry : timers.entrySet()) {
                reportCounter(timerEntry, data);
                reportSampling(timerEntry, 0.000001, data); // nanos -> millis
            }

            // Filter out unreportable entries.
            Collection<MetricDatum> nonEmptyData = Collections2.filter(data, new Predicate<MetricDatum>() {
                @Override
                public boolean apply(MetricDatum input) {
                    if (input == null) {
                        return false;
                    } else if (input.getStatisticValues() != null) {
                        // CloudWatch rejects any Statistic Sets with sample count == 0, which it probably should reject.
                        return input.getStatisticValues().getSampleCount() > 0;
                    }
                    return true;
                }
            });

            // Whether to use local "now" (true, new Date()) or cloudwatch service "now" (false, leave null).
            if (timestampLocal) {
                Date now = new Date();
                for (MetricDatum datum : nonEmptyData) {
                    datum.withTimestamp(now);
                }
            }

            // Finally, apply any user-level filter.
            Collection<MetricDatum> filtered = Collections2.filter(nonEmptyData, reporterFilter);

            // Each CloudWatch API request may contain at maximum 20 datums. Break into partitions of 20.
            Iterable<List<MetricDatum>> dataPartitions = Iterables.partition(filtered, 20);
            List<Future<?>> cloudWatchFutures = Lists.newArrayListWithExpectedSize(filtered.size());

            // Submit asynchronously with threads.
            for (List<MetricDatum> dataSubset : dataPartitions) {
                cloudWatchFutures.add(cloudWatch.putMetricDataAsync(new PutMetricDataRequest()
                        .withNamespace(metricNamespace)
                        .withMetricData(dataSubset)));
            }

            // Wait for CloudWatch putMetricData futures to be fulfilled.
            for (Future<?> cloudWatchFuture : cloudWatchFutures) {
                // We can't let an exception leak out of here, or else the reporter will cease running as described in
                // java.util.concurrent.ScheduledExecutorService.scheduleAtFixedRate(Runnable, long, long, TimeUnit unit)
                try {
                    // See what happened in case of an error.
                    cloudWatchFuture.get();
                } catch (Exception e) {
                    LOG.error("Exception reporting metrics to CloudWatch. The data in this CloudWatch API request " +
                            "may have been discarded, did not make it to CloudWatch.", e);
                }
            }

            LOG.debug("Sent {} metric data to CloudWatch. namespace: {}", filtered.size(), metricNamespace);

        } catch (RuntimeException e) {
            LOG.error("Error marshalling CloudWatch metrics.", e);
        }
    }


    void reportGauge(Map.Entry<String, Gauge> gaugeEntry, List<MetricDatum> data) {
        Gauge gauge = gaugeEntry.getValue();

        Object valueObj = gauge.getValue();
        if (valueObj == null) {
            return;
        }

        String valueStr = valueObj.toString();
        if (NumberUtils.isNumber(valueStr)) {
            final Number value = NumberUtils.createNumber(valueStr);

            String nameAndDimensions = StringUtils.substringBeforeLast(gaugeEntry.getKey(), NAME_STORAGE_RESOLUTION_TOKEN);
            String resolutionAndTimestamp = StringUtils.substringAfterLast(gaugeEntry.getKey(), NAME_STORAGE_RESOLUTION_TOKEN);
            final String resolution = StringUtils.substringBeforeLast(resolutionAndTimestamp, NAME_TIMESTAMP_TOKEN);
            final String timestamp = StringUtils.substringAfterLast(resolutionAndTimestamp, NAME_TIMESTAMP_TOKEN);

            DemuxedKey key = new DemuxedKey(appendGlobalDimensions(nameAndDimensions));
            Iterables.addAll(data, key.newDatums(new Function<MetricDatum, MetricDatum>() {
                @Override
                public MetricDatum apply(MetricDatum datum) {
                    return datum.withValue(value.doubleValue())
                            .withStorageResolution(Integer.valueOf(resolution))
                            .withTimestamp(new Date(Long.parseLong(timestamp)));
                }
            }));
        }
    }

    void reportCounter(Map.Entry<String, ? extends Counting> entry, List<MetricDatum> data) {
        Counting metric = entry.getValue();
        final long diff = diffLast(metric);
        if (diff == 0) {
            return;
        }

        String groupedName = entry.getKey();
        String counterName;
        final String timestamp;
        if (StringUtils.contains(groupedName, NAME_SAMPLING_TOKEN)) {
            counterName = StringUtils.substringBetween(groupedName, NAME_COUNTER_TOKEN, NAME_SAMPLING_TOKEN);
            timestamp = StringUtils.substringBetween(groupedName, NAME_TIMESTAMP_TOKEN, NAME_UNIT_TOKEN);
        } else {
            counterName = StringUtils.substringBetween(groupedName, NAME_COUNTER_TOKEN, NAME_METRIC_DIMENSION_SEPARATOR);
            timestamp = StringUtils.substringAfterLast(groupedName, NAME_TIMESTAMP_TOKEN);
        }

        if (counterName == null || counterName.equals("null")) {
            return;
        }

        String dimensions = StringUtils.substringBetween(groupedName, NAME_METRIC_DIMENSION_SEPARATOR, NAME_STORAGE_RESOLUTION_TOKEN);
        final String resolution = StringUtils.substringBetween(groupedName, NAME_STORAGE_RESOLUTION_TOKEN, NAME_TIMESTAMP_TOKEN);

        String nameAndDimensions = counterName + NAME_TOKEN_DELIMITER + dimensions;

        DemuxedKey key = new DemuxedKey(appendGlobalDimensions(nameAndDimensions));
        Iterables.addAll(data, key.newDatums(new Function<MetricDatum, MetricDatum>() {
            @Override
            public MetricDatum apply(MetricDatum datum) {
                return datum.withValue((double) diff)
                        .withUnit(StandardUnit.Count)
                        .withStorageResolution(Integer.valueOf(resolution))
                        .withTimestamp(new Date(Long.parseLong(timestamp)));
            }
        }));
    }

    /**
     * @param rescale the submitted sum by this multiplier. 1.0 is the identity (no rescale).
     */
    void reportSampling(Map.Entry<String, ? extends Sampling> entry, double rescale, List<MetricDatum> data) {
        Sampling metric = entry.getValue();
        Snapshot snapshot = metric.getSnapshot();
        double scaledSum = sum(snapshot.getValues()) * rescale;
        final StatisticSet statisticSet = new StatisticSet()
                .withSum(scaledSum)
                .withSampleCount((double) snapshot.size())
                .withMinimum((double) snapshot.getMin() * rescale)
                .withMaximum((double) snapshot.getMax() * rescale);

        String groupedName = entry.getKey();
        String samplingName = StringUtils.substringBetween(groupedName, NAME_SAMPLING_TOKEN, NAME_METRIC_DIMENSION_SEPARATOR);
        String dimensions = StringUtils.substringBetween(groupedName, NAME_METRIC_DIMENSION_SEPARATOR, NAME_STORAGE_RESOLUTION_TOKEN);
        final String resolution = StringUtils.substringBetween(groupedName, NAME_STORAGE_RESOLUTION_TOKEN, NAME_TIMESTAMP_TOKEN);
        final String timestamp = StringUtils.substringBetween(groupedName, NAME_TIMESTAMP_TOKEN, NAME_UNIT_TOKEN);
        final String unit = StringUtils.substringAfterLast(groupedName, NAME_UNIT_TOKEN);

        String nameAndDimensions = samplingName + NAME_TOKEN_DELIMITER + dimensions;

        DemuxedKey key = new DemuxedKey(appendGlobalDimensions(nameAndDimensions));
        Iterables.addAll(data, key.newDatums(new Function<MetricDatum, MetricDatum>() {
            @Override
            public MetricDatum apply(MetricDatum datum) {
                return datum.withStatisticValues(statisticSet).withUnit(unit)
                        .withStorageResolution(Integer.valueOf(resolution))
                        .withTimestamp(new Date(Long.parseLong(timestamp)));
            }
        }));
    }


    private long diffLast(Counting metric) {
        long count = metric.getCount();

        Long lastCount = lastPolledCounts.get(metric);
        lastPolledCounts.put(metric, count);

        if (lastCount == null) {
            lastCount = 0L;
        }
        return count - lastCount;
    }

    private long sum(long[] values) {
        long sum = 0L;
        for (long value : values) sum += value;
        return sum;
    }


    private String appendGlobalDimensions(String metric) {
        if (StringUtils.isBlank(StringUtils.trim(dimensions))) {
            return metric;
        } else {
            return metric + NAME_TOKEN_DELIMITER + dimensions;
        }
    }

}
