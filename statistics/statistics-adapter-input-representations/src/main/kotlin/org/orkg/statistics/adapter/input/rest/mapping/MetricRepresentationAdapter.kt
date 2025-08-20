package org.orkg.statistics.adapter.input.rest.mapping

import org.orkg.statistics.adapter.input.rest.DefaultMetricRepresentation
import org.orkg.statistics.adapter.input.rest.MetricRepresentation
import org.orkg.statistics.adapter.input.rest.ParameterSpecRepresentation
import org.orkg.statistics.adapter.input.rest.SlimMetricRepresentation
import org.orkg.statistics.adapter.input.rest.mapping.MetricRepresentationAdapter.MetricResponseFormat
import org.orkg.statistics.domain.Metric

private val responseFormatParameterSpec = ParameterSpecRepresentation(
    id = "response_format",
    name = "Response format",
    description = "The format returned by this endpoint.",
    type = "ResponseFormat",
    multivalued = false,
    values = MetricResponseFormat.entries
)

interface MetricRepresentationAdapter : ParameterSpecRepresentationAdapter {
    fun Metric.toMetricRepresentation(format: MetricResponseFormat, value: Number): MetricRepresentation =
        when (format) {
            MetricResponseFormat.DEFAULT -> {
                DefaultMetricRepresentation(
                    name = name,
                    description = description,
                    group = group,
                    value = value,
                    parameters = parameterSpecs.entries.map { (id, spec) -> spec.toParameterSpecRepresentation(id) } + responseFormatParameterSpec
                )
            }
            MetricResponseFormat.SLIM -> SlimMetricRepresentation(value)
        }

    enum class MetricResponseFormat {
        DEFAULT,
        SLIM,
    }
}
