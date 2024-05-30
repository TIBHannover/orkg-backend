package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.HeadVersion
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

private const val MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Comparison)-[:RELATED]->(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField)"""
private const val MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Comparison)-[:RELATED]->(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField {id: $id})"""
private const val INCLUDING_SUBFIELDS = """<-[:RELATED* 0.. {predicate_id: 'P36'}]-(:ResearchField {id: $id})"""
private const val WHERE_VISIBILITY_IS_LISTED = """WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED") AND node.created_at IS NOT NULL"""
private const val WHERE_VISIBILITY = """WHERE node.visibility = $visibility AND node.created_at IS NOT NULL"""
private const val WITH_DISTINCT_NODE = """WITH DISTINCT node"""

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jComparisonRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""
MATCH (cmp:Comparison:Resource {id: $id})-[:RELATED*1.. {predicate_id: "hasPreviousVersion"}]->(prev:Comparison)
RETURN prev.id AS id, prev.label AS label, prev.created_at AS createdAt, prev.created_by AS createdBy""")
    fun findVersionHistory(id: ThingId): List<HeadVersion>

    @Query("""$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedComparisonsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedComparisonsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllComparisonsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllComparisonsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}
