package org.orkg.statistics.adapter.input.rest

import java.net.URI

data class MetricRepresentation(
    val name: String,
    val description: String,
    val group: String,
    val value: Number,
    val parameters: List<ParameterSpecRepresentation>,
)

data class ParameterSpecRepresentation(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
)

data class EndpointReference(
    val href: URI,
)
