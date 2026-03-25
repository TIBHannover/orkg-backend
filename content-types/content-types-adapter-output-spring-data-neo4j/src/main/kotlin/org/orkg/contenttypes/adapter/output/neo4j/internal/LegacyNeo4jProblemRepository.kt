package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_CLASS
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_PREDICATE
import org.orkg.graph.adapter.output.neo4j.DATASET_CLASS
import org.orkg.graph.adapter.output.neo4j.DATASET_PREDICATE
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val DATASET_ID = $$"$datasetId"
private const val ID = $$"$id"

private const val PAGE_PARAMS = $$":#{orderBy(#pageable)} SKIP $skip LIMIT $limit"

interface LegacyNeo4jProblemRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""MATCH (node:Problem:Resource {id: $ID}) RETURN node""")
    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query(
        value = """MATCH (ds:$DATASET_CLASS {id: $DATASET_ID})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    RETURN DISTINCT problem ORDER BY problem.id $PAGE_PARAMS""",
        countQuery = """MATCH (ds:$DATASET_CLASS {id: $DATASET_ID})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    RETURN COUNT(DISTINCT problem) as cnt""",
    )
    fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Neo4jResource>
}
