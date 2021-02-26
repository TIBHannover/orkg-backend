package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TrendingResearchProblems
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.ChangeLog
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.TopContributorsWithProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

    fun getFieldsStats(): Map<String, Int>

    fun getObservatoryPapersCount(id: ObservatoryId): Long

    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

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
     * Get trending research problems
     */
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
}
