package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.services.ChangeLog
import eu.tib.orkg.prototype.statements.services.TopContributorsWithProfile
import eu.tib.orkg.prototype.statements.services.TopContributorsWithProfileAndTotalCount
import eu.tib.orkg.prototype.statements.spi.ObservatoryResources
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
    fun getFieldsStats(): Map<String, Int>

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
    fun getObservatoriesPapersAndComparisonsCount(): Iterable<ObservatoryResources>

    /**
     * Get top contributors
     */
    fun getTopCurrentContributors(pageable: Pageable, days: Long): Page<TopContributorsWithProfile>

    /**
     * Get recent changes in ORKG
     */
    fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog>

    /**
     * Get recent changes in ORKG by research field
     */
    fun getRecentChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLog>

    /**
     * Get trending research problems
     */
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    /**
     * Get top contributors by research field ID
     */
    fun getTopCurrentContributorsByResearchField(id: ResourceId, days: Long): Iterable<TopContributorsWithProfileAndTotalCount>

    /**
     * Get top contributors by research field ID excluding sub research fields
     */
    fun getTopCurrentContributorsByResearchFieldExcludeSubFields(id: ResourceId, days: Long): Iterable<TopContributorsWithProfileAndTotalCount>
}
