package eu.tib.orkg.prototype.statements.ports

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

// This is mapped to the result obtained from UNION of several contributions
typealias ResultObject = Map<String, Any?>
interface StatsRepository {

    fun getGraphMetaData(): Iterable<Map<String, Any?>>

    fun getResearchFieldsCount(): Long

    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    fun getObservatoryPapersCount(id: ObservatoryId): Long

    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    fun getObservatoriesPapersAndComparisonsCount(): Iterable<ObservatoryResources>

    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<TopContributorIdentifiers>

    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ResourceId, date: String): Iterable<Map<String, Iterable<ResultObject>>>

    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>

    fun getChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLogResponse>

    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>


}


/**
 * Data class for fetching
 * field statistics
 */
data class FieldsStats(
    val fieldId: String,
    val field: String,
    val papers: Long
)

/**
 * Data class for fetching
 * Observatory resources
 */
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
data class TrendingResearchProblems(
    val id: String,
    val researchProblem: String,
    val papersCount: Long? = 0
)

/**
 * Data class comprising of contributor ID
 * and the number of contributions
 * per contributor
 */
data class TopContributorIdentifiers(
    val id: String,
    val contributions: Long
)


