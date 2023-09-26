package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"
private const val problemId = "${'$'}problemId"

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jAuthorRepository :
    Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""
MATCH (c:Comparison:Resource {id: $id})-[rel:RELATED {predicate_id: 'compareContribution'}]->(cont:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper:Resource)-[:RELATED {predicate_id: 'hasAuthors'}]->(l:List)-[r:RELATED {predicate_id: "hasListElement"}]->(a:Thing)
OPTIONAL MATCH (p)-[:RELATED {predicate_id: 'P29'}]->(y:Literal)
WITH DISTINCT p.id AS id, a, toInteger(y.label) AS year, r.index AS index
WITH a.label AS authorLabel, COLLECT({paper: id, index: index, year: year}) AS info, apoc.coll.avg(COLLECT(index)) AS rank, COLLECT(a) AS authorResource
ORDER BY SIZE(info) DESC, rank ASC, authorLabel ASC
RETURN authorLabel, info, authorResource[0] AS authorResource $PAGE_PARAMS""",
        countQuery = """
MATCH (c:Comparison:Resource {id: $id})-[rel:RELATED {predicate_id: 'compareContribution'}]->(cont:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper:Resource)-[:RELATED {predicate_id: 'hasAuthors'}]->(l:List)-[r:RELATED {predicate_id: "hasListElement"}]->(a:Thing)
WITH a.label as authorLabel
RETURN COUNT(DISTINCT authorLabel) as cnt""")
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<Neo4jAuthorOfComparison>

    @Query(value = """
MATCH (problem:Problem:Resource {id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(author:Thing)
RETURN DISTINCT author.label AS author, author AS thing, COUNT(paper.id) AS papers
ORDER BY papers DESC, author $PAGE_PARAMS""",
        countQuery = """
MATCH (problem:Problem:Resource {id: $problemId})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution:Resource)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper:Resource)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(author:Thing)
WITH DISTINCT author.label AS author, author AS thing
RETURN COUNT(author)""")
    fun findAuthorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<Neo4jAuthorPerProblem>
}

data class Neo4jAuthorOfComparison(
    val authorLabel: String,
    val info: List<Neo4jAuthorInfo>,
    val authorResource: Neo4jThing
) {
    data class Neo4jAuthorInfo(
        // FIXME: The paper should be a Neo4jResource, but SDN 5.3 doesn't support this kind of internal mapping.
        // FIXME: Should be fixed using @Field or @Result with SDN 6+
        val paper: String,
        val index: Long,
        val year: Long?,
    )
}
