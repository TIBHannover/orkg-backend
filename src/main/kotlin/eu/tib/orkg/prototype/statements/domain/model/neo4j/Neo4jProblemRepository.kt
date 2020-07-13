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

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, cont, datetime() AS now
                    WHERE datetime(cont.created_at).year = now.year AND datetime(cont.created_at).month <= now.month AND datetime(cont.created_at).month > now.month - {0}
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt  DESC
                    LIMIT 5""")
    fun getTopResearchProblemsGoingBack(months: Int): Iterable<Neo4jResource>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt DESC
                    LIMIT 5""")
    fun getTopResearchProblemsAllTime(): Iterable<Neo4jResource>

    @Query("""MATCH (problem:Problem {resource_id: {0}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
WHERE contribution.created_by IS NOT NULL
RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
ORDER BY freq DESC""")
    fun getUsersLeaderboardPerProblem(problemId: ResourceId): Iterable<ContributorPerProblem>
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
        get() { return user == "00000000-0000-0000-0000-000000000000" }
}
