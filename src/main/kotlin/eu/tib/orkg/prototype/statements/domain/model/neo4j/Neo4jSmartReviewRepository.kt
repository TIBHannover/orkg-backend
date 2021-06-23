package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

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

private const val MATCH_FEATURED_SMART_REVIEW =
    """MATCH (node) WHERE EXISTS(node.featured) AND node.featured = true AND ANY(collectionFields IN ['SmartReviewPublished'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_NONFEATURED_SMART_REVIEW =
    """MATCH (node) WHERE (NOT EXISTS(node.featured) OR node.featured = false) AND ANY(collectionFields IN ['SmartReviewPublished'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNLISTED_SMART_REVIEW =
    """MATCH (node) WHERE EXISTS(node.unlisted) AND node.unlisted = true AND ANY(collectionFields IN ['SmartReviewPublished'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_LISTED_SMART_REVIEW =
    """MATCH (node) WHERE (NOT EXISTS(node.unlisted) OR node.unlisted = false) AND ANY(collectionFields IN ['SmartReviewPublished'] WHERE collectionFields IN LABELS(node))"""


private const val MATCH_SMART_REVIEW_BY_ID = """MATCH (node:`Resource`:`SmartReviewPublished` {resource_id: {0}})"""

interface Neo4jSmartReviewRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_SMART_REVIEW_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findSmartReviewByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_FEATURED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedSmartReviews(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_NONFEATURED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedSmartReviews(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNLISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedSmartReviews(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_SMART_REVIEW $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedSmartReviews(pageable: Pageable): Page<Neo4jResource>
}
