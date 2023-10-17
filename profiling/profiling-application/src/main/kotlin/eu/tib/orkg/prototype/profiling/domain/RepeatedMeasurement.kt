package eu.tib.orkg.prototype.profiling.domain

data class RepeatedMeasurement(
    val millis: List<Long>,
    val parameters: Map<String, Any>
)
