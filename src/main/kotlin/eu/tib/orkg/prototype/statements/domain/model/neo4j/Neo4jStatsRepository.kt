package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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

    @Query("""MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'ProblemStatement' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > {0} ) RETURN DISTINCT sub.created_by AS id, count(sub) AS contributions""",
    countQuery = "MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'ProblemStatement' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > {0} ) RETURN DISTINCT COUNT(sub.created_by) AS cnt")
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<TopContributorIdentifiers>

    /**
     * This query fetches the contributor IDs from sub research fields as well.
     */
    @Query("""MATCH (research:ResearchField)<-[:RELATED* {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: {0}}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND (p.created_by <> '00000000-0000-0000-0000-000000000000' AND p.created_at > {1}) RETURN DISTINCT p.created_by AS id, COUNT(DISTINCT p) AS contributions""",
    countQuery = "MATCH (research:ResearchField)<-[:RELATED* {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: {0}}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND (p.created_by <> '00000000-0000-0000-0000-000000000000' AND p.created_at > {1}) RETURN COUNT(DISTINCT p.created_by) AS cnt")
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ResourceId, date: String, pageable: Pageable): Page<TopContributorIdentifiers>

    @Query("""MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (NOT 'PaperDeleted' IN  labels(sub)) AND (NOT 'ContributionDeleted' IN labels(sub)) RETURN sub.resource_id AS id, sub.label AS label, sub.created_at AS createdAt, COALESCE(sub.created_by, '00000000-0000-0000-0000-000000000000') as createdBy, labels(sub) AS classes""",
    countQuery = "MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (NOT 'PaperDeleted' IN  labels(sub)) AND (NOT 'ContributionDeleted' IN labels(sub)) RETURN count(sub.created_by) as cnt")
    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (v:Visualization)-[:RELATED]->(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED]->(r:ResearchField{resource_id: {0}}) WHERE (NOT 'PaperDeleted' IN  labels(p) AND NOT 'ContributionDeleted' IN labels(contribution))  WITH COLLECT([v.resource_id,v.label, v.created_at, v.created_by,labels(v)])  + COLLECT([comp.resource_id,comp.label, comp.created_at, comp.created_by, labels(comp)]) + COLLECT([contribution.resource_id,contribution.label, contribution.created_at, contribution.created_by, labels(contribution)]) + COLLECT([p.resource_id,p.label, p.created_at, p.created_by, labels(p)]) + COLLECT([r.resource_id,r.label, r.created_at, r.created_by, labels(r)]) AS items UNWIND items AS changelogs RETURN DISTINCT changelogs[0] AS id, changelogs[1] AS label, changelogs[2] AS createdAt, changelogs[3] AS createdBy, changelogs[4] AS classes""",
    countQuery = "MATCH (v:Visualization)-[:RELATED]->(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED]->(r:ResearchField{resource_id: {0}}) WHERE (NOT 'PaperDeleted' IN  labels(p) AND NOT 'ContributionDeleted' IN labels(contribution))  WITH COLLECT([v.resource_id,v.label, v.created_at, v.created_by,labels(v)])  + COLLECT([comp.resource_id,comp.label, comp.created_at, comp.created_by, labels(comp)]) + COLLECT([contribution.resource_id,contribution.label, contribution.created_at, contribution.created_by, labels(contribution)]) + COLLECT([p.resource_id,p.label, p.created_at, p.created_by, labels(p)]) + COLLECT([r.resource_id,r.label, r.created_at, r.created_by, labels(r)]) AS items UNWIND items AS changelogs RETURN COUNT(changelogs) AS cnt")
    fun getChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount""",
    countQuery = "MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
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
data class TopContributorIdentifiers(
    val id: String,
    val contributions: Long
)
