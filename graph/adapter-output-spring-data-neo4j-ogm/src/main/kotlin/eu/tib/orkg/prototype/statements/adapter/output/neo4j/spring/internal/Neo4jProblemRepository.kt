package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_CLASS
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_PREDICATE
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.DATASET_CLASS
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.DATASET_PREDICATE
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

/**
 * Partial query that returns the node.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE = """RETURN node"""

private const val RETURN_NODE_COUNT = """RETURN count(node)"""

private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at"""

private const val datasetId = "${'$'}datasetId"
private const val months = "${'$'}months"
private const val problemId = "${'$'}problemId"
private const val id = "${'$'}id"
private const val visibility = "${'$'}visibility"

private const val MATCH_PROBLEM = """MATCH (node:`Resource`:`Problem`)"""

private const val WHERE_VISIBILITY = """WHERE COALESCE(node.visibility, "DEFAULT") = $visibility"""

private const val WHERE_VISIBILITY_IS_LISTED = """WHERE (node.visibility IS NULL OR node.visibility = "FEATURED")"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val WITH_DISTINCT_NODE = """WITH DISTINCT node"""

private const val MATCH_LISTED_PROBLEM = """$MATCH_PROBLEM WHERE (node.visibility IS NULL OR node.visibility = "FEATURED")"""
private const val MATCH_CONTRIBUTION_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)"""
private const val MATCH_PAPER_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(node:Paper:Resource)"""
private const val MATCH_RESEARCH_FIELD_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(node:ResearchField:Resource)"""
private const val MATCH_COMPARISON_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'compareContribution'}]-(node:Comparison:Resource)"""
private const val MATCH_LITERATURE_LISTS_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField:Resource)<-[:RELATED {predicate_id: 'HasList'}]-(node:LiteratureList:Resource)"""
private const val MATCH_SMART_REVIEWS_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(node:SmartReview:Resource)"""
private const val MATCH_VISUALIZATIONS_RELATED_TO_PROBLEM_WITH_ID = """MATCH (:Problem:Resource {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)-[:RELATED {predicate_id: 'hasVisualization'}]->(node:Visualization:Resource)"""

interface Neo4jProblemRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (node:Problem:Resource {resource_id: $id}) RETURN node""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    // Contributions

    @Query("""$MATCH_CONTRIBUTION_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_CONTRIBUTION_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedContributionsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_CONTRIBUTION_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_CONTRIBUTION_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllContributionsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Papers

    @Query("""$MATCH_PAPER_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_PAPER_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedPapersByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_PAPER_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllPapersByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Research Fields

    @Query("""$MATCH_RESEARCH_FIELD_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_RESEARCH_FIELD_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedResearchFieldsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_RESEARCH_FIELD_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_RESEARCH_FIELD_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllResearchFieldsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Comparisons

    @Query("""$MATCH_COMPARISON_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedComparisonsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_COMPARISON_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_COMPARISON_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllComparisonsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Literature Lists

    @Query("""$MATCH_LITERATURE_LISTS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LITERATURE_LISTS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedLiteratureListsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LITERATURE_LISTS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LITERATURE_LISTS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllLiteratureListsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Smart Reviews

    @Query("""$MATCH_SMART_REVIEWS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_SMART_REVIEWS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedSmartReviewsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_SMART_REVIEWS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_SMART_REVIEWS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllSmartReviewsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    // Visualizations

    @Query("""$MATCH_VISUALIZATIONS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_VISUALIZATIONS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY_IS_LISTED $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllListedVisualizationsByProblem(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_VISUALIZATIONS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_VISUALIZATIONS_RELATED_TO_PROBLEM_WITH_ID $WHERE_VISIBILITY $WITH_DISTINCT_NODE $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT""")
    fun findAllVisualizationsByProblemAndVisibility(id: ResourceId, visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (:Problem:Resource {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField:Resource)
                    RETURN field, COUNT(paper) AS freq
                    ORDER BY freq DESC""")
    fun findResearchFieldsPerProblem(problemId: ResourceId): Iterable<Neo4jFieldPerProblem>

    @Query("""MATCH (problem:Problem:Resource)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution:Resource)
                    WITH problem, cont, datetime() AS now
                    WHERE datetime(cont.created_at).year = now.year AND datetime(cont.created_at).month <= now.month AND datetime(cont.created_at).month > now.month - $months
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt  DESC
                    LIMIT 5""")
    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Neo4jResource>

    @Query("""MATCH (problem:Problem:Resource)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution:Resource)
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt DESC
                    LIMIT 5""")
    fun findTopResearchProblemsAllTime(): Iterable<Neo4jResource>

    @Query(value = """MATCH (problem:Problem:Resource {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution:Resource)
                        WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                        RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                        ORDER BY freq DESC""",
        countQuery = """MATCH (problem:Problem:Resource {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution:Resource)
                    WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                    WITH contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                    RETURN COUNT(user)""")
    fun findContributorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<Neo4jContributorPerProblem>

    @Query(value = """MATCH (ds:$DATASET_CLASS {resource_id: $datasetId})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    RETURN DISTINCT problem""",
        countQuery = """MATCH (ds:$DATASET_CLASS {resource_id: $datasetId})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution:Resource)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem:Resource)
                    RETURN COUNT(DISTINCT problem) as cnt""")
    fun findResearchProblemForDataset(datasetId: ResourceId, pageable: Pageable): Page<Neo4jResource>
    
    @Query("""$MATCH_LISTED_PROBLEM $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_PROBLEM $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedProblems(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PROBLEM $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_PROBLEM $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllProblemsByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>
}

@QueryResult
data class Neo4jFieldPerProblem(
    val field: Neo4jResource,
    val freq: Long
)

@QueryResult
data class Neo4jContributorPerProblem(
    val user: String,
    val freq: Long
)
