package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonVersion
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

interface Neo4jComparisonRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""
MATCH (cmp:Comparison:Resource {id: $id})-[:RELATED*1.. {predicate_id: "hasPreviousVersion"}]->(prev:Comparison)
RETURN prev.id AS id, prev.label AS label, prev.created_at AS createdAt""")
    fun findVersionHistory(id: ThingId): List<ComparisonVersion>
}
