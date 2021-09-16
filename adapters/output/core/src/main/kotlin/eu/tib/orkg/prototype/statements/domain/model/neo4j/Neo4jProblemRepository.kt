package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.datasetId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.limit
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.problemId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.skip
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional
import java.util.UUID

interface Neo4jProblemRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (node:Problem {resource_id: $id}) RETURN node""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    @Query("""MATCH (:Problem {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)
                    RETURN field, COUNT(paper) AS freq
                    ORDER BY freq DESC""")
    fun findResearchFieldsPerProblem(problemId: ResourceId): Iterable<FieldPerProblem>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, cont, datetime() AS now
                    WHERE datetime(cont.created_at).year = now.year AND datetime(cont.created_at).month <= now.month AND datetime(cont.created_at).month > now.month - ${'$'}months
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

    @Query(value = """MATCH (problem:Problem {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
                        WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                        RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                        ORDER BY freq DESC SKIP $skip LIMIT $limit""",
    countQuery = """MATCH (problem:Problem {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
                    WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                    WITH contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                    RETURN COUNT(user)""")
    fun findContributorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<ContributorPerProblem>

    @Query(value = """MATCH (problem:Problem {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        RETURN author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
                        ORDER BY papers DESC, author SKIP $skip LIMIT $limit""",
        countQuery = """MATCH (problem:Problem {resource_id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        WITH author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
                        RETURN COUNT (author)""")
    // TODO: Should group on the resource and not on the label. See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/172#note_378465870
    fun findAuthorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<AuthorPerProblem>

    @Query(value = """MATCH (ds:Dataset {resource_id: $datasetId})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem)
                    RETURN DISTINCT problem""")
    fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Neo4jResource>
}

data class FieldPerProblem(
    val field: Neo4jResource,
    val freq: Long
)

data class ContributorPerProblem(
    val user: String,
    val freq: Long
) {
    val contributor: UUID = UUID.fromString(user)
    val isAnonymous: Boolean
        get() = contributor == UUID(0, 0)
}

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
