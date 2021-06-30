package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TrendingResearchProblems
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.ChangeLog
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.TopContributorsWithProfile
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.TopContributorsWithProfileAndTotalCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

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
    fun getTopCurrentContributors(pageable: Pageable): Page<TopContributorsWithProfile>

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
     * Get Top 5 papers recently modified
     */
    fun getTopTrendingPapers(): Map<String, String>

}
