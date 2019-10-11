package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jStatsRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""CALL apoc.meta.stats()""")
    fun getGraphMetaData(): Iterable<HashMap<String, Any>>

    @Query("""Match()-[:RELATES_TO{predicate_id:'P31'}]->(cont) WITH COUNT(cont) as contibutions RETURN contibutions""")
    fun getContributionsCount(): Long

    @Query("""MATCH (n:Resource{resource_id: 'R11'}) CALL apoc.path.subgraphAll(n, {relationshipFilter: '>'}) YIELD nodes UNWIND nodes as field WITH COUNT(field) as cnt RETURN cnt""")
    fun getResearchFieldsCount(): Long

    @Query("""MATCH ()-[{predicate_id:'P32'}]->(n) RETURN COUNT(DISTINCT n.resource_id)""")
    fun getResearchProblemsCount(): Long
}
