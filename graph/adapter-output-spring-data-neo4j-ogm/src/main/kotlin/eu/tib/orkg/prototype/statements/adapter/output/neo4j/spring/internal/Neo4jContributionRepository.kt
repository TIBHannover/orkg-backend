package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

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

private const val MATCH_CONTRIBUTION = """MATCH (node:`Resource`:`Contribution`)"""

private const val MATCH_FEATURED_CONTRIBUTION =
    """$MATCH_CONTRIBUTION WHERE node.featured IS NOT NULL AND node.featured = true"""

private const val MATCH_NONFEATURED_CONTRIBUTION =
    """$MATCH_CONTRIBUTION WHERE (node.featured IS NULL OR node.featured = false)"""

private const val MATCH_UNLISTED_CONTRIBUTION =
    """$MATCH_CONTRIBUTION WHERE node.unlisted IS NOT NULL AND node.unlisted = true"""

private const val MATCH_LISTED_CONTRIBUTION =
    """$MATCH_CONTRIBUTION WHERE (node.unlisted IS NULL OR node.unlisted = false)"""

private const val MATCH_CONTRIBUTION_BY_ID = """MATCH (node:`Resource`:`Contribution` {resource_id: $id})"""

interface Neo4jContributionRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""$MATCH_CONTRIBUTION_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findContributionByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_FEATURED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedContributions(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_NONFEATURED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedContributions(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNLISTED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedContributions(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_CONTRIBUTION $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedContributions(pageable: Pageable): Page<Neo4jResource>
}
