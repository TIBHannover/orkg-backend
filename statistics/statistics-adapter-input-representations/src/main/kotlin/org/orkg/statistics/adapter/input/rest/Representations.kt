package org.orkg.statistics.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI

interface MetricRepresentation {
    val value: Number
}

data class SlimMetricRepresentation(
    override val value: Number,
) : MetricRepresentation

data class DefaultMetricRepresentation(
    val name: String,
    val description: String,
    val group: String,
    override val value: Number,
    val parameters: List<ParameterSpecRepresentation<*>>,
) : MetricRepresentation

data class ParameterSpecRepresentation<T>(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val multivalued: Boolean,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val values: List<T>,
)

data class EndpointReference(
    val href: URI,
)
