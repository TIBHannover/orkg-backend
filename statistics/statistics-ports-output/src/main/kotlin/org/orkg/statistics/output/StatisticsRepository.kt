package org.orkg.statistics.output

interface StatisticsRepository {
    fun countNodes(label: String): Long

    fun countRelations(type: String): Long

    /**
     * Unused nodes are nodes that do not have any incoming relations
     */
    fun countUnusedNodes(label: String): Long

    /**
     * Orphan nodes are nodes that do not have any relations
     */
    fun countOrphanNodes(label: String): Long
}
