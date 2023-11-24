package org.orkg.contenttypes.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"
private const val visibility = "${'$'}visibility"

/**
 * Partial query that returns the node.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE = """RETURN node"""

/**
 * Partial query that returns the node count for use in count queries.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE_COUNT = """RETURN count(node)"""

/**
 * Partial query that expands the node properties so that they can be used with pagination in custom queries.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.id AS id, node.created_at AS created_at"""

private const val MATCH_SMART_REVIEW = """MATCH (node:`Resource`:`SmartReviewPublished`)"""

private const val MATCH_SMART_REVIEW_BY_ID = """MATCH (node:`Resource`:`SmartReviewPublished` {id: $id})"""

private const val WHERE_VISIBILITY = """WHERE node.visibility = $visibility AND node.created_at IS NOT NULL"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val MATCH_LISTED_SMART_REVIEW = """$MATCH_SMART_REVIEW WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED") AND node.created_at IS NOT NULL"""

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jSmartReviewRepository :
    Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""$MATCH_SMART_REVIEW_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findSmartReviewByResourceId(id: ThingId): Optional<Neo4jResource>

    @Query("""$MATCH_LISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedSmartReviews(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_SMART_REVIEW $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_SMART_REVIEW $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllSmartReviewsByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}
