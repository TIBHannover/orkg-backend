package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.transaction.annotation.Transactional

private const val classes = "${'$'}classes"
private const val label = "${'$'}label"
private const val id = "${'$'}id"
private const val visibility = "${'$'}visibility"
private const val verified = "${'$'}verified"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

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

// Custom queries

private const val HAS_CLASSES = """ANY(collectionFields IN $classes WHERE collectionFields IN LABELS(node))"""

private const val MATCH_PAPER = """MATCH (node:`Resource`:`Paper`)"""
private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {id: $id})"""

private const val WHERE_VISIBILITY = """WHERE node.visibility = $visibility AND node.created_at IS NOT NULL"""

private const val VERIFIED_IS = """COALESCE(node.verified, false) = $verified"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val MATCH_LISTED_RESOURCE = """MATCH (node:Resource) WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED") AND node.created_at IS NOT NULL"""

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, ThingId> {
    override fun existsById(id: ThingId): Boolean

    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE NOT node:PaperDeleted AND toLower(node.label) = toLower($label) $RETURN_NODE LIMIT 1""")
    fun findPaperByLabel(label: String?): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE NOT node:PaperDeleted AND toLower(node.label) = toLower($label) $RETURN_NODE""")
    fun findAllPapersByLabel(label: String): Iterable<Neo4jResource>

    @Query("""$MATCH_PAPER_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findPaperById(id: ThingId): Optional<Neo4jResource>

    @Query(value = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN DISTINCT n.created_by ORDER BY n.created_by ASC $ORDER_BY_PAGE_PARAMS""",
        countQuery = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN COUNT(DISTINCT n.created_by) as cnt""")
    fun findAllContributorIds(pageable: Pageable): Page<String> // FIXME: This should be ContributorId

    @Transactional
    override fun deleteById(id: ThingId)

    @Query("""$MATCH_PAPER WHERE $VERIFIED_IS $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $ORDER_BY_PAGE_PARAMS""",
        countQuery = """$MATCH_PAPER WHERE $VERIFIED_IS $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $ORDER_BY_PAGE_PARAMS""",
        countQuery = """MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility?, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $ORDER_BY_PAGE_PARAMS""",
        countQuery = """$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $ORDER_BY_PAGE_PARAMS""",
        countQuery = """MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility?, id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE $ORDER_BY_PAGE_PARAMS""",
        countQuery = """$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>
}
