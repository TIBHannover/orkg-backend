package org.orkg.statistics.output

interface StatisticsRepository {
    fun countNodes(label: String): Long

    fun countRelations(type: String): Long
}
