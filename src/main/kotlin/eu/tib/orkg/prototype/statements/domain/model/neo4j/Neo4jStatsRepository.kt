package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jStatsRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""CALL apoc.meta.stats()""")
    fun getGraphMetaData(): Iterable<HashMap<String, Any>>

    @Query("""MATCH (n:Resource{resource_id: 'R11'}) CALL apoc.path.subgraphAll(n, {relationshipFilter: '>'}) YIELD nodes UNWIND nodes as field WITH COUNT(field) as cnt RETURN cnt""")
    fun getResearchFieldsCount(): Long

    @Query("""MATCH (n:ResearchField) WITH n OPTIONAL MATCH (n)-[:RELATED*0..3 {predicate_id: 'P36'}]->(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper) RETURN n.resource_id AS fieldId, n.label AS field, COUNT(p) AS papers""")
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    @Query("""MATCH (n:Paper {observatory_id: {0}}) RETURN COUNT(n) As totalPapers""")
    fun getObservatoryPapersCount(id: ObservatoryId): Long

    @Query("""MATCH (n:Comparison {observatory_id: {0}}) RETURN COUNT(n) As totalComparisons""")
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    @Query("""MATCH (n:Paper) WHERE n.observatory_id<>'00000000-0000-0000-0000-000000000000' WITH DISTINCT (n.observatory_id) AS observatoryId, COUNT(n) AS resources OPTIONAL MATCH (c:Comparison) where c.observatory_id<>'00000000-0000-0000-0000-000000000000' AND c.observatory_id = observatoryId WITH DISTINCT (c.observatory_id) as cobservatoryId, count(c) as comparisons, resources, observatoryId RETURN observatoryId, resources, comparisons""")
    fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources>

    @Query("""MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'ProblemStatement' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > {0} ) RETURN DISTINCT sub.created_by AS id, count(sub) AS numberOfContributions""",
    countQuery = "MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'ProblemStatement' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > {0} ) RETURN DISTINCT COUNT(sub.created_by) AS cnt")
    fun getTopCurrentContributors(date: String, pageable: Pageable): Page<TopContributors>

    @Query("""MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000')  RETURN sub.resource_id AS id, sub.label AS label, sub.created_at AS createdAt, sub.created_by AS createdBy, labels(sub) AS classes""",
    countQuery = "MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000') RETURN count(sub.created_by) as cnt")
    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount""",
    countQuery = "MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
}

@QueryResult
data class FieldsStats(
    val fieldId: String,
    val field: String,
    val papers: Long
)

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
    val createdAt: String,
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
data class TopContributors(
    val id: String,
    val numberOfContributions: Long
)
