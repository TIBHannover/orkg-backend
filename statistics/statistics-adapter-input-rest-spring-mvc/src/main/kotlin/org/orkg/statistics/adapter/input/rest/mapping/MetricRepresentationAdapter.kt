package org.orkg.statistics.adapter.input.rest.mapping

import org.orkg.statistics.adapter.input.rest.MetricRepresentation
import org.orkg.statistics.domain.Metric

interface MetricRepresentationAdapter {
    fun Metric.toMetricRepresentation(): MetricRepresentation =
        MetricRepresentation(name, description, group, value())
}
