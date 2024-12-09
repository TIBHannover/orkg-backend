package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

interface Neo4jComparisonRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""
MATCH (h:Comparison)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(:ComparisonPublished {id: $id})
MATCH (h)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:ComparisonPublished)
RETURN h AS head, COLLECT(p) AS published""")
    fun findVersionHistoryForPublishedComparison(id: ThingId): Neo4jVersionInfo

    @Query("""
CALL {
    MATCH (node:ComparisonPublished:LatestVersion)
    WHERE (node.visibility = 'DEFAULT' OR node.visibility = 'FEATURED') AND NOT EXISTS((node)-[:`RELATED` {predicate_id: 'P26'}]->(:`Literal`))
    RETURN node
    UNION ALL
    MATCH (node:Comparison)
    WHERE (node.visibility = 'DEFAULT' OR node.visibility = 'FEATURED') AND NOT EXISTS((node)-[:RELATED {predicate_id: 'hasPublishedVersion'}]->(:ComparisonPublished))
    RETURN node
}
WITH node
RETURN node $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL {
    MATCH (node:ComparisonPublished:LatestVersion)
    WHERE (node.visibility = 'DEFAULT' OR node.visibility = 'FEATURED') AND NOT EXISTS((node)-[:`RELATED` {predicate_id: 'P26'}]->(:`Literal`))
    RETURN node
    UNION ALL
    MATCH (node:Comparison)
    WHERE (node.visibility = 'DEFAULT' OR node.visibility = 'FEATURED') AND NOT EXISTS((node)-[:RELATED {predicate_id: 'hasPublishedVersion'}]->(:ComparisonPublished))
    RETURN node
}
WITH node
RETURN COUNT(node)""")
    fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Neo4jResource>
}

data class Neo4jVersionInfo(
    val head: Neo4jResource,
    val published: List<Neo4jResource>
)
