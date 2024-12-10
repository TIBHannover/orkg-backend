package org.orkg.statistics.testing.fixtures

import org.orkg.statistics.domain.Metric
import org.orkg.statistics.domain.SimpleMetric

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
