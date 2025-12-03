package org.orkg.graph.input

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.LegacyContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyStatisticsUseCases {
    /**
     * Get paper count and comparison count
     */
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    /**
     * Get paper count and comparison count by observatory id
     */
    fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats

    /**
     * Get top contributors
     */
    fun getTopCurrentContributors(days: Long, pageable: Pageable): Page<LegacyContributorRecord>

    /**
     * Get top contributors by research field ID
     */
    fun getTopCurrentContributorsByResearchField(id: ThingId, days: Long, pageable: Pageable): Page<LegacyContributorRecord>

    /**
     * Get top contributors by research field ID excluding sub research fields
     */
    fun getTopCurrentContributorsByResearchFieldExcludeSubFields(
        id: ThingId,
        days: Long,
        pageable: Pageable,
    ): Page<LegacyContributorRecord>
}
