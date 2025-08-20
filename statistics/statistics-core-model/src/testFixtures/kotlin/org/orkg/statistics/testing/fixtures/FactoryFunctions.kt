package org.orkg.statistics.testing.fixtures

import org.orkg.statistics.domain.Metric
import org.orkg.statistics.domain.SimpleMetric
import org.orkg.statistics.domain.SingleValueParameterSpec

fun createMetrics(): List<Metric> = listOf(
    SimpleMetric(
        name = "metric1",
        description = "irrelevant1",
        group = "group1",
        supplier = { 1 }
    ),
    SimpleMetric(
        name = "metric2",
        description = "irrelevant2",
        group = "group1",
        supplier = { 2 }
    ),
    SimpleMetric(
        name = "metric3",
        description = "irrelevant3",
        group = "group2",
        supplier = { 3 }
    )
)

fun createSingleValueParameterSpec() = SingleValueParameterSpec(
    name = "parameter1",
    description = "Description of the parameter.",
    type = Int::class,
    values = listOf(0, 1, 2, 3),
    parser = { it.toInt() }
)

fun createSimpleMetric() = SimpleMetric(
    name = "metric1",
    description = "Description of the metric.",
    group = "group1",
    parameterSpecs = mapOf("filter" to createSingleValueParameterSpec()),
    supplier = { 1 }
)
