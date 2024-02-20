package org.orkg.statistics.adapter.input.rest

import java.net.URI

data class MetricRepresentation(
    val name: String,
    val description: String,
    val group: String,
    val value: Number
)

data class EndpointReference(
    val href: URI
)
