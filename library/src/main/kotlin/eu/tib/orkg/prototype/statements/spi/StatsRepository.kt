package eu.tib.orkg.prototype.statements.spi

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.QueryResult

// This is mapped to the result obtained from UNION of several contributions
typealias ResultObject = Map<String, Any?>

interface StatsRepository {
    fun getGraphMetaData(): Iterable<HashMap<String, Any>>
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>
    fun getObservatoryPapersCount(id: ObservatoryId): Long
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long
    fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources>
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<TopContributorIdentifiers>
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ThingId, date: String): List<List<Map<String, List<ResultObject>>>>
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ThingId, date: String): List<List<Map<String, List<ResultObject>>>>
    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLogResponse>
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
    fun getOrphanedNodesCount(): Long
}

/**
 * Data class for fetching
 * field statistics
 */
@QueryResult
data class FieldsStats(
    val fieldId: String,
    val field: String,
    val papers: Long
)

/**
 * Data class for fetching
 * Observatory resources
 */
@QueryResult
data class ObservatoryResources(
    @JsonProperty("observatory_id")
    val observatoryId: String,
    val resources: Long = 0,
    val comparisons: Long = 0
)

/**
 * Data class comprising of resource ID,
 * label, time of creation/modification,
 * creator and corresponding classes
 */
@QueryResult
data class ChangeLogResponse(
    val id: String,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("created_by")
    val createdBy: String,
    val classes: List<String>
)

/**
 * Data class comprising of resource ID,
 * research problem and total number of
 * papers per research problem
 */
@QueryResult
data class TrendingResearchProblems(
    val id: String,
    val researchProblem: String,
    val papersCount: Long
)

/**
 * Data class comprising of contributor ID
 * and the number of contributions
 * per contributor
 */
@QueryResult
data class TopContributorIdentifiers(
    val id: String,
    val contributions: Long
)
