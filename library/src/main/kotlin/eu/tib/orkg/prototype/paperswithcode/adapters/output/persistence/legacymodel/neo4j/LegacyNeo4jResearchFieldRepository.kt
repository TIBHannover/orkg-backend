package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface LegacyNeo4jResearchFieldRepository : Neo4jRepository<Neo4jResource, Long> {
    @Query(
        """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN DISTINCT r""",
        countQuery = """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN COUNT(DISTINCT r) AS cnt"""
    )
    fun findResearchFieldsWithBenchmarks(): Iterable<Neo4jResource>
}
