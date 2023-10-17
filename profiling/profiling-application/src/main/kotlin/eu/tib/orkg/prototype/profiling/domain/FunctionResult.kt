package eu.tib.orkg.prototype.profiling.domain

data class FunctionResult(
    val repositoryName: String,
    val functionName: String,
    val measurements: List<RepeatedMeasurement>
)
