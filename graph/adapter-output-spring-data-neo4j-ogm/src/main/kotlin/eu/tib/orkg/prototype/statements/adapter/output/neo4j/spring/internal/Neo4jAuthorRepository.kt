package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"
private const val problemId = "${'$'}problemId"

interface Neo4jAuthorRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (c:Comparison:Resource {id: $id})-[rel:RELATED {predicate_id: 'compareContribution'}]->(cont:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper:Resource)-[:RELATED {predicate_id: 'P27'}]->(a:Thing)
WITH a, p, collect(p.id) as paperIDs, count(p) as numPapers
MATCH (p:Paper:Resource)-[rel2:RELATED {predicate_id: 'P27'}]->(auth:Thing)
OPTIONAL MATCH(p)-[:RELATED {predicate_id: 'P29'}]->(year:Literal)
WITH a, paperIDs, numPapers, auth, p, year.label AS year
ORDER BY rel2.created_at
WITH a, paperIDs, numPapers, collect(auth) as authors, p, year
UNWIND authors as author
WITH a.label as authorLabel, paperIDs, numPapers, author, CASE WHEN 'Resource' IN LABELS(a) THEN a ELSE NULL END AS authorResource, p, year
UNWIND paperIDs as paperID
WITH authorLabel, paperID, numPapers, collect(DISTINCT author.label) as authors, authorResource, p, year
WITH authorLabel, paperID, numPapers, authorResource, p, year,
     CASE
       WHEN authorLabel IN authors THEN 
         [x in range(0,size(authors)-1) WHERE authors[x] = authorLabel][0] 
       ELSE -1
     END AS authorIndex
WITH authorLabel, collect(DISTINCT {paper: paperID, index: authorIndex, year: toInteger(year)}) as info, authorResource
RETURN authorLabel, info, authorResource""",
        countQuery = """
MATCH (c:Comparison:Resource {id: $id})-[rel:RELATED {predicate_id: 'compareContribution'}]->(cont:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper:Resource)-[:RELATED {predicate_id: 'P27'}]->(a:Thing)
WITH a.label as authorLabel
RETURN COUNT(DISTINCT authorLabel) as cnt""")
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<Neo4jAuthorOfComparison>

    @Query(value = """MATCH (problem:Problem:Resource {id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        RETURN author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.id) AS papers
                        ORDER BY papers DESC, author""",
        countQuery = """MATCH (problem:Problem:Resource {id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
                        WITH author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.id) AS papers
                        RETURN COUNT (author)""")
    fun findAuthorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<Neo4jAuthorPerProblem>
}

@QueryResult
data class Neo4jAuthorOfComparison(
    val authorLabel: String,
    val info: Iterable<Neo4jAuthorInfo>,
    val authorResource: Neo4jResource?
) {
    data class Neo4jAuthorInfo(
        // FIXME: The paper should be a Neo4jResource, but SDN 5.3 doesn't support this kind of internal mapping.
        // FIXME: Should be fixed using @Field or @Result with SDN 6+
        val paper: String,
        val index: Long,
        val year: Long?,
    )
}

@QueryResult
data class Neo4jAuthorPerProblem(
    val author: String,
    val thing: Neo4jThing,
    val papers: Long
)
