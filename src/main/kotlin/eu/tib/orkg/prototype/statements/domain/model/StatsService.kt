package eu.tib.orkg.prototype.statements.domain.model

import java.util.UUID

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

    fun getFieldsStats(): Map<String, Int>

    fun getObservatoryPapersCount(id: UUID): Long

    fun getObservatoryComparisonsCount(id: UUID): Long
}
