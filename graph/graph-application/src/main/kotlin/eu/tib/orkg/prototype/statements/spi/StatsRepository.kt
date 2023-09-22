package eu.tib.orkg.prototype.statements.spi

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.convert.ConvertWith

interface StatsRepository {
    fun getGraphMetaData(): Iterable<Map<String, Any?>>
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>
    fun getObservatoryPapersCount(id: ObservatoryId): Long
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>
    fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats
    fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): ResearchFieldStats
    fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord>
    fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord>
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord>
    fun getChangeLog(pageable: Pageable): Page<Resource>
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Resource>
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
    fun getOrphanedNodesCount(): Long
}

/**
 * Data class for fetching
 * field statistics
 */
data class FieldsStats(
    val fieldId: ThingId,
    val field: String,
    val papers: Long
)

/**
 * Data class for fetching
 * Observatory resources
 */
data class ObservatoryStats(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    val papers: Long = 0,
    val comparisons: Long = 0,
    val total: Long = 0
)

/**
 * Data class for fetching research field stats
 */
data class ResearchFieldStats(
    val id: ThingId,
    val papers: Long = 0,
    val comparisons: Long = 0,
    val total: Long = 0
)

/**
 * Data class comprising of resource ID,
 * research problem and total number of
 * papers per research problem
 */
data class TrendingResearchProblems(
    val id: ThingId,
    val researchProblem: String,
    val papersCount: Long
)
