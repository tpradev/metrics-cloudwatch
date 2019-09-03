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

import com.amazonaws.services.cloudwatch.model.Dimension;

public class Constants {

    /**
     * Delimiter of tokens in the metric name. Plain tokens will be retained as the CloudWatch "Metric Name".
     */
    public static final String NAME_TOKEN_DELIMITER_RGX = "\\s";
    // For building; should qualify against NAME_TOKEN_DELIMITER_RGX
    public static final String NAME_TOKEN_DELIMITER = " ";

    /**
     * Separator of key and value segments of a metric name. These segments will be split into the key and value of
     * a CloudWatch {@link Dimension}.
     */
    public static final String NAME_DIMENSION_SEPARATOR = "=";

    /**
     * If any token, whether a simple string or a dimension pair ends with this marker, then metrics will be sent once
     * with and once without.
     */
    public static final String NAME_PERMUTE_MARKER = "*";

    // Should line up with constants. Name should not contain any special character, and may optionally end with the
    // permute marker.
    public static final String VALID_NAME_TOKEN_RGX = "[^\\s=\\*]+\\*?";
    public static final String VALID_DIMENSION_PART_RGX = "[^\\s=\\*]+";

    //added
    public static final String NAME_METRIC_DIMENSION_SEPARATOR = ",";
    public static final String NAME_COUNTER_TOKEN = "CounterName=";
    public static final String NAME_SAMPLING_TOKEN = "SamplingName=";
    public static final String NAME_STORAGE_RESOLUTION_TOKEN = "StorageResolution=";
    public static final String NAME_TIMESTAMP_TOKEN = "Timestamp=";
    public static final String NAME_UNIT_TOKEN = "Unit=";

}
