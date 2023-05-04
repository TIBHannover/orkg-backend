package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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

private const val MATCH_VISUALIZATION = """MATCH (node:`Resource`:`Visualization`)"""

private const val MATCH_VISUALIZATION_BY_ID = """MATCH (node:`Resource`:`Visualization` {resource_id: $id})"""

private const val WHERE_VISIBILITY = """WHERE COALESCE(node.visibility, "DEFAULT") = $visibility"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val MATCH_LISTED_VISUALIZATION = """$MATCH_VISUALIZATION WHERE (node.visibility IS NULL OR node.visibility = "FEATURED")"""

interface Neo4jVisualizationRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_VISUALIZATION_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findVisualizationByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query("""$MATCH_LISTED_VISUALIZATION $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_VISUALIZATION $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedVisualizations(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_VISUALIZATION $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_VISUALIZATION $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllVisualizationsByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}
