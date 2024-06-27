package org.orkg.curation.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jClass
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.id AS id, node.created_at AS created_at"""

interface Neo4jClassCurationRepository : Neo4jRepository<Neo4jClass, ThingId> {
    @Query(
        value = """
                match (node:Class)
                WHERE NOT EXISTS((node)-[:RELATED {predicate_id: "description"}]->(:Literal))
                $WITH_NODE_PROPERTIES
                return node $ORDER_BY_PAGE_PARAMS
    
            """,
        countQuery = """
                match (node:Class)
                WHERE NOT EXISTS((node)-[:RELATED {predicate_id: "description"}]->(:Literal))
                return COUNT(node)
            """
    )
    fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Neo4jClass>

}
