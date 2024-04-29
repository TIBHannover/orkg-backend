package org.orkg.statistics.input

import org.orkg.statistics.domain.Metric

interface RetrieveStatisticsUseCase {
    fun findAllGroups(): List<String>

    fun findAllMetricsByGroup(group: String): List<Metric>

    fun findMetricByGroupAndName(group: String, metricName: String): Metric
}
