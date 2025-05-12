package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_CLASS
import org.orkg.graph.adapter.output.neo4j.BENCHMARK_PREDICATE
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val FIELD_ID = "${'$'}fieldId"
private const val ID = "${'$'}id"
private const val VISIBILITY = "${'$'}visibility"

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

private const val INCLUDING_SUBFIELDS = """<-[:RELATED* 0.. {predicate_id: 'P36'}]-(:ResearchField {id: $ID})"""
private const val WHERE_VISIBILITY_IS_LISTED = """WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED") AND node.created_at IS NOT NULL"""
private const val WHERE_VISIBILITY = """WHERE node.visibility = $VISIBILITY AND node.created_at IS NOT NULL"""
private const val WITH_DISTINCT_NODE = """WITH DISTINCT node"""
private const val MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Problem)<-[:RELATED]-(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField)"""
private const val MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Problem)<-[:RELATED]-(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField {id: $ID})"""

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jResearchFieldRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""MATCH (field:ResearchField:Resource {id: $ID}) RETURN field""")
    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query(
        """MATCH (field:ResearchField:Resource {id: $FIELD_ID})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    WITH COUNT(paper) AS papers, problem
                    RETURN DISTINCT problem, papers $PAGE_PARAMS""",
        countQuery = """MATCH (field:ResearchField:Resource {id: $FIELD_ID})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                        WITH COUNT(paper) AS papers, problem
                        RETURN COUNT(papers) AS cnt"""
    )
    fun findAllPaperCountsPerResearchProblem(fieldId: ThingId, pageable: Pageable): Page<Neo4jProblemsPerField>

    @Query(
        """MATCH (research:ResearchField:Resource)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField:Resource{id: $ID}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN DISTINCT orkgusers $PAGE_PARAMS""",
        countQuery = """MATCH (research:ResearchField:Resource)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField:Resource{id: $ID}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN COUNT(DISTINCT orkgusers) AS cnt"""
    )
    fun findAllContributorIdsIncludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>

    @Query(
        """MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField:Resource{id: $ID}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN orkgusers $PAGE_PARAMS""",
        countQuery = """MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField:Resource{id: $ID}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN COUNT(orkgusers) as cnt"""
    )
    fun findAllContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>

    @Query(
        """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN DISTINCT r $PAGE_PARAMS""",
        countQuery = """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN COUNT(DISTINCT r) AS cnt"""
    )
    fun findAllWithBenchmarks(pageable: Pageable): Page<Neo4jResource>

    // Problems

    @Query(
        """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WITH_DISTINCT_NODE $WHERE_VISIBILITY_IS_LISTED $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WITH_DISTINCT_NODE $WHERE_VISIBILITY_IS_LISTED $RETURN_NODE_COUNT"""
    )
    fun findAllListedProblemsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query(
        """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WITH_DISTINCT_NODE $WHERE_VISIBILITY_IS_LISTED $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WITH_DISTINCT_NODE $WHERE_VISIBILITY_IS_LISTED $RETURN_NODE_COUNT"""
    )
    fun findAllListedProblemsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query(
        """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WITH_DISTINCT_NODE $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WITH_DISTINCT_NODE $WHERE_VISIBILITY $RETURN_NODE_COUNT"""
    )
    fun findAllProblemsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query(
        """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WITH_DISTINCT_NODE $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WITH_DISTINCT_NODE $WHERE_VISIBILITY $RETURN_NODE_COUNT"""
    )
    fun findAllProblemsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}

data class Neo4jProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long,
)
