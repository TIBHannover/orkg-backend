package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

/**
 * Partial query that returns the node as well as its ID and relationships.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE =
    """RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)"""

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

private const val MATCH_FEATURED_COMPARISON =
    """MATCH (node) WHERE EXISTS(node.featured) AND node.featured = true AND ANY(collectionFields IN ['Comparison'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_NONFEATURED_COMPARISON =
    """MATCH (node) WHERE (NOT EXISTS(node.featured) OR node.featured = false) AND ANY(collectionFields IN ['Comparison'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNLISTED_COMPARISON =
    """MATCH (node) WHERE EXISTS(node.unlisted) AND node.unlisted = true AND ANY(collectionFields IN ['Comparison'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_LISTED_COMPARISON =
    """MATCH (node) WHERE (NOT EXISTS(node.unlisted) OR node.unlisted = false) AND ANY(collectionFields IN ['Comparison'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_COMPARISON_BY_ID = """MATCH (node:`Resource`:`Comparison` {resource_id: $id})"""

interface Neo4jComparisonRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_COMPARISON_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findComparisonByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_FEATURED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedComparisons(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_NONFEATURED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedComparisons(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNLISTED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedComparisons(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_COMPARISON}$WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_COMPARISON $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedComparisons(pageable: Pageable): Page<Neo4jResource>
}