package org.orkg.statistics.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.statistics.testing.fixtures.createDummyMetrics

internal class StatisticsServiceUnitTest {

    private val metrics = createDummyMetrics()
    private val service = StatisticsService(metrics)

    @Test
    fun `given several metrics, when groups are fetched, they are returned`() {
        service.findAllGroups() shouldBe listOf("group1", "group2")
    }

    @Test
    fun `given several metrics, when fetching by group, they are returned`() {
        service.findAllMetricsByGroup("group1") shouldBe metrics.filter { it.group == "group1" }
    }

    @Test
    fun `given several metrics, when fetching by group, but group does not exist, it throws an exception`() {
        shouldThrow<GroupNotFound> { service.findAllMetricsByGroup("missing") }
    }

    @Test
    fun `given a metric, when fetched by group and name, they are returned`() {
        service.findMetricByGroupAndName("group1", "metric1") shouldBe metrics.first()
    }

    @Test
    fun `given a metric, when fetched by group and name, but group does not exist, it throws an exception`() {
        shouldThrow<GroupNotFound> { service.findMetricByGroupAndName("missing", "metric1") }
    }

    @Test
    fun `given a metric, when fetched by group and name, but metric name does not exist within group, it throws an exception`() {
        shouldThrow<MetricNotFound> { service.findMetricByGroupAndName("group1", "metric3") }
    }
}
