package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val RETURN_NODE =
    """RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)"""

private const val RETURN_NODE_COUNT = """RETURN count(node)"""

private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at"""

private const val MATCH_FEATURED_PROBLEM =
    """MATCH (node) WHERE node.featured = true AND ANY(collectionFields IN ['Problem'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_NONFEATURED_PROBLEM =
    """MATCH (node) WHERE node.featured = false AND ANY(collectionFields IN ['Problem'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNLISTED_PROBLEM =
    """MATCH (node) WHERE node.unlisted = true AND ANY(collectionFields IN ['Problem'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_LISTED_PROBLEM =
    """MATCH (node) WHERE node.unlisted = false AND ANY(collectionFields IN ['Problem'] WHERE collectionFields IN LABELS(node))"""

interface Neo4jProblemRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (node:Problem {resource_id: {0}}) RETURN node""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    @Query("""MATCH (:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)
                    RETURN field, COUNT(paper) AS freq
                    ORDER BY freq DESC""")
    fun findResearchFieldsPerProblem(problemId: ResourceId): Iterable<FieldPerProblem>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, cont, datetime() AS now
                    WHERE datetime(cont.created_at).year = now.year AND datetime(cont.created_at).month <= now.month AND datetime(cont.created_at).month > now.month - {0}
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt  DESC
                    LIMIT 5""")
    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Neo4jResource>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt DESC
                    LIMIT 5""")
    fun findTopResearchProblemsAllTime(): Iterable<Neo4jResource>

    @Query(value = """MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
                        WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                        RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                        ORDER BY freq DESC""",
    countQuery = """MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
                    WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                    WITH contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                    RETURN COUNT(user)""")
    fun findContributorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<ContributorPerProblem>

    @Query(value = """MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        RETURN author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
                        ORDER BY papers DESC, author""",
        countQuery = """MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        WITH author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
                        RETURN COUNT (author)""")
    // TODO: Should group on the resource and not on the label. See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/172#note_378465870
    fun findAuthorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<AuthorPerProblem>

    @Query(value = """MATCH (ds:Dataset {resource_id: {0}})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem)
                    RETURN DISTINCT problem""")
    fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_FEATURED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedProblems(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_NONFEATURED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedProblems(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNLISTED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedProblems(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_PROBLEM $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedProblems(pageable: Pageable): Page<Neo4jResource>
}

@QueryResult
data class FieldPerProblem(
    val field: Neo4jResource,
    val freq: Long
)

@QueryResult
data class ContributorPerProblem(
    val user: String,
    val freq: Long
) {
    val contributor: UUID = UUID.fromString(user)
    val isAnonymous: Boolean
        get() = contributor == UUID(0, 0)
}

@QueryResult
data class AuthorPerProblem(
    val author: String,
    val thing: Neo4jThing,
    val papers: Long
) {
    val isLiteral: Boolean
        get() = thing is Neo4jLiteral
    val toAuthorResource: Neo4jResource
        get() = thing as Neo4jResource
}
