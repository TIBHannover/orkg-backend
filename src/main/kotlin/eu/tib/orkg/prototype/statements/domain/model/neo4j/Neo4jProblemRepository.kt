package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.UUID
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jProblemRepository :
    Neo4jRepository<Neo4jStatement, Long> {

    @Query("""MATCH (:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)
                    RETURN field, COUNT(paper) AS freq
                    ORDER BY freq DESC""")
    fun getResearchFieldsPerProblem(problemId: ResourceId): Iterable<FieldPerProblem>

    @Query("""MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
WHERE contribution.created_by IS NOT NULL
RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
ORDER BY freq DESC""")
    fun getUsersLeaderboardPerProblem(problemId: ResourceId): Iterable<ContributorPerProblem>

    @Query("""MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
RETURN author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
ORDER BY papers DESC, author""")
    // TODO: Should group on the resource and not on the label. See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/172#note_378465870
    fun getAuthorsLeaderboardPerProblem(problemId: ResourceId): Iterable<AuthorPerProblem>
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
        get() = user == "00000000-0000-0000-0000-000000000000"
}

@QueryResult
data class AuthorPerProblem(
    val author: String,
    val thing: Neo4jThing,
    val papers: Long
) {
    val isLiteral: Boolean
        get() = thing is Neo4jLiteral
    val authorResource: Neo4jResource
        get() = thing as Neo4jResource
}
