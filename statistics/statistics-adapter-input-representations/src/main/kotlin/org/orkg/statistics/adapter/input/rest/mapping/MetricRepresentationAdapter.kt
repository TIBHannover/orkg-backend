package org.orkg.statistics.adapter.input.rest.mapping

import org.orkg.statistics.adapter.input.rest.MetricRepresentation
import org.orkg.statistics.domain.Metric

interface MetricRepresentationAdapter : ParameterSpecRepresentationAdapter {
    fun Metric.toMetricRepresentation(value: Number): MetricRepresentation =
        MetricRepresentation(
            name = name,
            description = description,
            group = group,
            value = value,
            parameters = parameterSpecs.entries.map { (id, spec) -> spec.toParameterSpecRepresentation(id) }
        )
}
