package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

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
    """WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at"""

private const val MATCH_COMPARISON = """MATCH (node:`Resource`:`Comparison`)"""

private const val MATCH_COMPARISON_BY_ID = """MATCH (node:`Resource`:`Comparison` {resource_id: $id})"""

private const val WHERE_VISIBILITY = """WHERE COALESCE(node.visibility, "DEFAULT") = $visibility"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val MATCH_LISTED_COMPARISON = """$MATCH_COMPARISON WHERE (node.visibility IS NULL OR node.visibility = "FEATURED")"""

interface Neo4jComparisonRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_COMPARISON_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findComparisonByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query("""$MATCH_LISTED_COMPARISON $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_COMPARISON $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedComparisons(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_COMPARISON $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_COMPARISON $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllComparisonsByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}
