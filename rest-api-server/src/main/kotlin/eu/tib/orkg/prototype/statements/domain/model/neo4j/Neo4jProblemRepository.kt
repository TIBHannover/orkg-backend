package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_CLASS
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_PREDICATE
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.DATASET_CLASS
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.DATASET_PREDICATE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import java.util.UUID
import org.neo4j.ogm.annotation.Property
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

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)
                    RETURN c.resource_id as id, c.label as label, c.created_at as created_at, c.featured as featured, c.unlisted as unlisted, LABELS(c) as classes, c.created_by as createdBy""",
    countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)
                    RETURN COUNT(c)""")
    fun findContributionsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)
                    RETURN c.resource_id as id, c.label as label, c.created_at as created_at, c.featured as featured, c.unlisted as unlisted, LABELS(c) as classes, c.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)
                    RETURN COUNT(c)""")
    fun findContributionsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)
                    WITH DISTINCT paper RETURN paper.resource_id as id, paper.label as label, paper.created_at as created_at, paper.featured as featured, paper.unlisted as unlisted, LABELS(paper) as classes, paper.created_by as createdBy""",
    countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)
                    RETURN COUNT(DISTINCT paper)""")
    fun findPapersByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)
                    WITH DISTINCT paper RETURN paper.resource_id as id, paper.label as label, paper.created_at as created_at, paper.featured as featured, paper.unlisted as unlisted, LABELS(paper) as classes, paper.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(c:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)
                    RETURN COUNT(DISTINCT paper)""")
    fun findPapersByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)
                    RETURN f.resource_id as id, f.label as label, f.created_at as created_at, f.featured as featured, f.unlisted as unlisted, LABELS(f) as classes, f.created_by as createdBy""",
    countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)
                    RETURN COUNT(f)""")
    fun findResearchFieldsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)
                    RETURN f.resource_id as id, f.label as label, f.created_at as created_at, f.featured as featured, f.unlisted as unlisted, LABELS(f) as classes, f.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)
                    RETURN COUNT(f)""")
    fun findResearchFieldsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'compareContribution'}]-(c:Comparison)
                    RETURN DISTINCT c.resource_id as id, c.label as label, c.created_at as created_at, c.featured as featured, c.unlisted as unlisted, LABELS(c) as classes, c.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'compareContribution'}]-(c:Comparison)
                    RETURN COUNT(DISTINCT c)""")
    fun findComparisonsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'compareContribution'}]-(c:Comparison)
                    RETURN DISTINCT c.resource_id as id, c.label as label, c.created_at as created_at, c.featured as featured, c.unlisted as unlisted, LABELS(c) as classes, c.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'compareContribution'}]-(c:Comparison)
                    RETURN COUNT(DISTINCT c)""")
    fun findComparisonsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)<-[:RELATED{predicate_id: 'HasList'}]-(l:LiteratureList)
                    RETURN DISTINCT l.resource_id as id, l.label as label, l.created_at as created_at, l.featured as featured, l.unlisted as unlisted, LABELS(l) as classes, l.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)<-[:RELATED{predicate_id: 'HasList'}]-(l:LiteratureList)
                    RETURN COUNT(DISTINCT l)""")
    fun findLiteratureListsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(f:ResearchField)<-[:RELATED{predicate_id: 'HasList'}]-(l:LiteratureList)
                    RETURN DISTINCT l.resource_id as id, l.label as label, l.created_at as created_at, l.featured as featured, l.unlisted as unlisted, LABELS(l) as classes, l.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)<-[:RELATED{predicate_id: 'HasList'}]-(l:LiteratureList)
                    RETURN COUNT(DISTINCT l)""")
    fun findLiteratureListsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(s:SmartReview)
                    RETURN DISTINCT s.resource_id as id, s.label as label, s.created_at as created_at, s.featured as featured, s.unlisted as unlisted, LABELS(s) as classes, s.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(s:SmartReview)
                    RETURN COUNT(DISTINCT s)""")
    fun findSmartReviewsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(s:SmartReview)
                    RETURN DISTINCT s.resource_id as id, s.label as label, s.created_at as created_at, s.featured as featured, s.unlisted as unlisted, LABELS(s) as classes, s.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(s:SmartReview)
                    RETURN COUNT(DISTINCT s)""")
    fun findSmartReviewsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)-[:RELATED {predicate_id: 'hasVisualization'}]->(v:Visualization)
                    RETURN DISTINCT v.resource_id as id, v.label as label, v.created_at as created_at, v.featured as featured, v.unlisted as unlisted, LABELS(v) as classes, v.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, featured:{1}, unlisted:{2}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)-[:RELATED {predicate_id: 'hasVisualization'}]->(v:Visualization)
                    RETURN COUNT(DISTINCT v)""")
    fun findVisualizationsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    @Query("""MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)-[:RELATED {predicate_id: 'hasVisualization'}]->(v:Visualization)
                    RETURN DISTINCT v.resource_id as id, v.label as label, v.created_at as created_at, v.featured as featured, v.unlisted as unlisted, LABELS(v) as classes, v.created_by as createdBy""",
        countQuery = """MATCH (p:Problem {resource_id: {0}, unlisted:{1}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)-[:RELATED {predicate_id: 'hasVisualization'}]->(v:Visualization)
                    RETURN COUNT(DISTINCT v)""")
    fun findVisualizationsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>

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

    @Query(value = """MATCH (ds:$DATASET_CLASS {resource_id: {0}})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem)
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
data class DetailsPerProblem(
    val id: String?,
    val label: String?,
    @JsonProperty("created_at")
    @Property("created_at")
    val createdAt: String?,
    val featured: Boolean?,
    val unlisted: Boolean?,
    val classes: List<String>,
    @JsonProperty("created_by")
    val createdBy: String?
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
