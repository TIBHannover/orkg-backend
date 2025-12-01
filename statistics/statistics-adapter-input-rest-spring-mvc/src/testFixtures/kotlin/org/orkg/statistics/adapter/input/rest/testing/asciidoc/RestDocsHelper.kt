package org.orkg.statistics.adapter.input.rest.testing.asciidoc

import org.orkg.statistics.adapter.input.rest.mapping.MetricRepresentationAdapter.MetricResponseFormat

val allowedMetricResponseFormatValues =
    MetricResponseFormat.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")
