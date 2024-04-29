package org.orkg.statistics.domain

import org.orkg.statistics.input.RetrieveStatisticsUseCase
import org.springframework.stereotype.Service

@Service
class StatisticsService(metrics: List<Metric>) : RetrieveStatisticsUseCase {
    private val metrics: Map<String, List<Metric>> = metrics
        .groupBy { it.group }
        .mapValues { (_, value) -> value.sortedBy { it.name } }

    override fun findAllGroups(): List<String> =
        metrics.keys.sorted()

    override fun findAllMetricsByGroup(group: String): List<Metric> =
        metrics[group] ?: throw GroupNotFound(group)

    override fun findMetricByGroupAndName(group: String, metricName: String): Metric =
        findAllMetricsByGroup(group).singleOrNull { it.name == metricName } ?: throw MetricNotFound(group, metricName)
}
