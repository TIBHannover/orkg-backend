package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.*
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val fieldId = "${'$'}fieldId"
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

private const val INCLUDING_SUBFIELDS = """<-[:RELATED* 0.. {predicate_id: 'P36'}]-(:ResearchField {id: $id})"""
private const val WHERE_VISIBILITY_IS_LISTED = """WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED") AND node.created_at IS NOT NULL"""
private const val WHERE_VISIBILITY = """WHERE node.visibility = $visibility AND node.created_at IS NOT NULL"""
private const val WITH_DISTINCT_NODE = """WITH DISTINCT node"""
private const val MATCH_PAPER_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Paper)-[:RELATED]->(:ResearchField)"""
private const val MATCH_PAPER_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Paper)-[:RELATED]->(:ResearchField {id: $id})"""
private const val MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Comparison)-[:RELATED]->(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField)"""
private const val MATCH_COMPARISON_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Comparison)-[:RELATED]->(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField {id: $id})"""
private const val MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Problem)<-[:RELATED]-(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField)"""
private const val MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Problem)<-[:RELATED]-(:Contribution)<-[:RELATED]-(:Paper)-[:RELATED]->(:ResearchField {id: $id})"""
private const val MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD = """MATCH (node:Visualization)<-[:RELATED]-(:Comparison)-[:RELATED]->(:ResearchField)"""
private const val MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:Visualization)<-[:RELATED]-(:Comparison)-[:RELATED]->(:ResearchField {id: $id})"""
private const val MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD = """MATCH (node:SmartReviewPublished)-[:RELATED]->(:SmartReview)-[:RELATED]->(:ResearchField)"""
private const val MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:SmartReviewPublished)-[:RELATED]->(:SmartReview)-[:RELATED]->(:ResearchField {id: $id})"""
private const val MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD = """MATCH (node:LiteratureListPublished)-[:RELATED]->(:LiteratureList)-[:RELATED]->(:ResearchField)"""
private const val MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD_WITH_ID = """MATCH (node:LiteratureListPublished)-[:RELATED]->(:LiteratureList)-[:RELATED]->(:ResearchField {id: $id})"""

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jResearchFieldRepository :
    Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""MATCH (field:ResearchField:Resource {id: $id}) RETURN field""")
    override fun findById(id: ThingId): Optional<Neo4jResource>

    @Query("""MATCH (field:ResearchField:Resource {id: $fieldId})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    WITH COUNT(paper) AS papers, problem
                    RETURN DISTINCT problem, papers $PAGE_PARAMS""",
        countQuery = """MATCH (field:ResearchField:Resource {id: $fieldId})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                        WITH COUNT(paper) AS papers, problem
                        RETURN COUNT(papers) AS cnt"""
    )
    fun getResearchProblemsOfField(fieldId: ThingId, pageable: Pageable): Page<Neo4jProblemsPerField>

    @Query("""MATCH (research:ResearchField:Resource)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField:Resource{id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN DISTINCT orkgusers $PAGE_PARAMS""",
        countQuery = """MATCH (research:ResearchField:Resource)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField:Resource{id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN COUNT(DISTINCT orkgusers) AS cnt""")
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ThingId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField:Resource{id: $id}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN orkgusers $PAGE_PARAMS""",
        countQuery = """MATCH(comparison1: Comparison:Resource)-[related:RELATED]->(contribution1:Contribution:Resource)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField:Resource{id: $id}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN COUNT(orkgusers) as cnt""")
    fun getContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN DISTINCT r $PAGE_PARAMS""",
        countQuery = """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField:Resource) RETURN COUNT(DISTINCT r) AS cnt""")
    fun findResearchFieldsWithBenchmarks(pageable: Pageable): Page<Neo4jResource>

    // Papers

    @Query("""$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedPapersByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedPapersByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllPapersByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PAPER_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllPapersByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Comparisons

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

    // Problems

    @Query("""$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedProblemsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedProblemsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllProblemsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_PROBLEM_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllProblemsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Visualizations

    @Query("""$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedVisualizationsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedVisualizationsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllVisualizationsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_VISUALIZATION_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllVisualizationsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Smart Reviews

    @Query("""$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedSmartReviewsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedSmartReviewsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllSmartReviewsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_SMART_REVIEW_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllSmartReviewsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Literature Lists

    @Query("""$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedLiteratureListsByResearchFieldIncludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllListedLiteratureListsByResearchFieldExcludingSubFields(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD$INCLUDING_SUBFIELDS $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllLiteratureListsByResearchFieldAndVisibilityIncludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LITERATURE_LIST_RELATED_TO_RESEARCH_FIELD_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $RETURN_NODE_COUNT""")
    fun findAllLiteratureListsByResearchFieldAndVisibilityExcludingSubFields(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}

data class Neo4jProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long
)
