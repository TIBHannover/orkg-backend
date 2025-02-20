package org.orkg.profiling.domain

data class FunctionResult(
    val repositoryName: String,
    val functionName: String,
    val measurements: List<RepeatedMeasurement>,
)
