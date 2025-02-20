package org.orkg.profiling.domain

data class RepeatedMeasurement(
    val millis: List<Long>,
    val parameters: Map<String, Any>,
)
