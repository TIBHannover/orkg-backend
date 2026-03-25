package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_CLASS
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_PREDICATE
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val ID = $$"$id"

private const val PAGE_PARAMS = $$":#{orderBy(#pageable)} SKIP $skip LIMIT $limit"

interface LegacyNeo4jResearchFieldRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""MATCH (field:ResearchField:Resource {id: $ID}) RETURN field""")
    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query(
        """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN DISTINCT r $PAGE_PARAMS""",
        countQuery = """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN COUNT(DISTINCT r) AS cnt""",
    )
    fun findAllWithBenchmarks(pageable: Pageable): Page<Neo4jResource>
}
