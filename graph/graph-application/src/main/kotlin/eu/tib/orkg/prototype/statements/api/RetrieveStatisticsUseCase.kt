package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ChangeLog
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
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

    data class ContributorRecord(
        val contributor: ContributorId,
        val comparisons: Long = 0,
        val papers: Long = 0,
        val contributions: Long = 0,
        val problems: Long = 0,
        val visualizations: Long = 0,
        val total: Long = 0
    )
}
