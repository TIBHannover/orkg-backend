package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
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

    @Query("""MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.label AS research_problem, COUNT(paper) AS paper_cnt, COLLECT(DISTINCT paper.created_by) AS contributor RETURN research_problem ORDER BY size(contributor) DESC LIMIT 5""")
    fun getTrendingResearchProblems(): List<String>

    @Query("""MATCH (sub: Thing) WHERE 'Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub) RETURN sub.label ORDER BY sub.created_at DESC LIMIT 5""")
    fun getChangeLog(): List<String>

    @Query("""MATCH(sub: Resource) WHERE sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > {0} AND ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'ProblemStatement' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub))  RETURN DISTINCT sub.created_by AS contributors LIMIT 5""")
    fun getTopCurrentContributors(date: String): List<ContributorId>
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
