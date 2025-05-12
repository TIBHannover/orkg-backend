package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val LABEL = "${'$'}label"
private const val ID = "${'$'}id"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

/**
 * Partial query that returns the node.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE = """RETURN node"""

/**
 * Partial query that expands the node properties so that they can be used with pagination in custom queries.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.id AS id, node.created_at AS created_at"""

// Custom queries

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {id: $ID})"""

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, ThingId> {
    override fun existsById(id: ThingId): Boolean

    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE NOT node:PaperDeleted AND toLower(node.label) = toLower($LABEL) $RETURN_NODE LIMIT 1""")
    fun findPaperByLabel(label: String?): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE NOT node:PaperDeleted AND toLower(node.label) = toLower($LABEL) $RETURN_NODE""")
    fun findAllPapersByLabel(label: String): List<Neo4jResource>

    @Query("""$MATCH_PAPER_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findPaperById(id: ThingId): Optional<Neo4jResource>

    @Query(
        value = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN DISTINCT n.created_by AS id ORDER BY n.created_by ASC $ORDER_BY_PAGE_PARAMS""",
        countQuery = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN COUNT(DISTINCT n.created_by) as cnt"""
    )
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>

    override fun deleteById(id: ThingId)
}
