package org.orkg.graph.input

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ChangeLog
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.domain.Stats
import org.orkg.graph.domain.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveStatisticsUseCase {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(extra: List<String>?): Stats

    /**
     * Get paper count for each research field
     */
    fun getFieldsStats(): Map<ThingId, Int>

    /**
     * Get paper count by observatory ID
     */
    fun getObservatoryPapersCount(id: ObservatoryId): Long

    /**
     * Get comparison count by observatory ID
     */
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    /**
     * Get paper count and comparison count
     */
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    /**
     * Get paper count and comparison count by observatory id
     */
    fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats

    /**
     * Get paper count and comparison count by research field id. Optionally accounts for subfields.
     */
    fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): ResearchFieldStats

    /**
     * Get top contributors
     */
    fun getTopCurrentContributors(days: Long, pageable: Pageable): Page<ContributorRecord>

    /**
     * Get recent changes in ORKG
     */
    fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog>

    /**
     * Get recent changes in ORKG by research field
     */
    fun getRecentChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLog>

    /**
     * Get trending research problems
     */
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    /**
     * Get top contributors by research field ID
     */
    fun getTopCurrentContributorsByResearchField(id: ThingId, days: Long, pageable: Pageable): Page<ContributorRecord>

    /**
     * Get top contributors by research field ID excluding sub research fields
     */
    fun getTopCurrentContributorsByResearchFieldExcludeSubFields(
        id: ThingId,
        days: Long,
        pageable: Pageable
    ): Page<ContributorRecord>
}
