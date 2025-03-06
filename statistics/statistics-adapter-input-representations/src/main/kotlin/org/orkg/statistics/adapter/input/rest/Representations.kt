package org.orkg.statistics.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI

data class MetricRepresentation(
    val name: String,
    val description: String,
    val group: String,
    val value: Number,
    val parameters: List<ParameterSpecRepresentation<*>>,
)

data class ParameterSpecRepresentation<T>(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val values: List<T>,
)

data class EndpointReference(
    val href: URI,
)
