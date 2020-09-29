package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID
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
    fun getObservatoryPapersCount(id: UUID): Long

    @Query("""MATCH (n:Comparison {observatory_id: {0}}) RETURN COUNT(n) As totalComparisons""")
    fun getObservatoryComparisonsCount(id: UUID): Long

    @Query("""MATCH (n:Paper) where n.observatory_id<>'00000000-0000-0000-0000-000000000000' WITH DISTINCT (n.observatory_id) as observatoryId, count(*) as resources MATCH (c:Comparison) where c.observatory_id<>'00000000-0000-0000-0000-000000000000' AND c.observatory_id = observatoryId WITH DISTINCT (c.observatory_id) as observatoryId, count(*) as comparisons, resources RETURN observatoryId, resources, comparisons""")
    fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources>
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
