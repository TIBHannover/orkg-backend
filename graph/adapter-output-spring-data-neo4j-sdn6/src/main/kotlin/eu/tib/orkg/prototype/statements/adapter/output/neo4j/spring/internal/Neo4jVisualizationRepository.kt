package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

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

private const val MATCH_FEATURED_VISUALIZATION =
    """$MATCH_VISUALIZATION WHERE node.featured IS NOT NULL AND node.featured = true"""

private const val MATCH_NONFEATURED_VISUALIZATION =
    """$MATCH_VISUALIZATION WHERE (node.featured IS NULL OR node.featured = false)"""

private const val MATCH_UNLISTED_VISUALIZATION =
    """$MATCH_VISUALIZATION WHERE node.unlisted IS NOT NULL AND node.unlisted = true"""

private const val MATCH_LISTED_VISUALIZATION =
    """$MATCH_VISUALIZATION WHERE (node.unlisted IS NULL OR node.unlisted = false)"""

private const val MATCH_VISUALIZATION_BY_ID = """MATCH (node:`Resource`:`Visualization` {resource_id: $id})"""

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jVisualizationRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_VISUALIZATION_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findVisualizationByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_FEATURED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedVisualizations(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_NONFEATURED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedVisualizations(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_UNLISTED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedVisualizations(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LISTED_VISUALIZATION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedVisualizations(pageable: Pageable): Page<Neo4jResource>
}
