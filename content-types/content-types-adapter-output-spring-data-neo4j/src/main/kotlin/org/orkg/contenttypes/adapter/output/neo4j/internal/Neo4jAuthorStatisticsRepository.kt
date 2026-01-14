package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AuthorRecord
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val AFTER = "${'$'}after"
private const val BEFORE = "${'$'}before"
private const val ID = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

interface Neo4jAuthorStatisticsRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query(
"""
MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(:Problem:Thing {id: $ID})
OPTIONAL MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
WITH ppr, cmp, vsl
WHERE (ppr IS NULL OR ppr.created_at > $AFTER AND ppr.created_at < $BEFORE) AND (cmp IS NULL OR cmp.created_at > $AFTER AND cmp.created_at < $BEFORE) AND (vsl IS NULL OR vsl.created_at > $AFTER AND vsl.created_at < $BEFORE)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(ppra:Thing)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(cmpa:Thing)
OPTIONAL MATCH (vsl)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(vsla:Thing)
WITH [
    [ppra, [1, 0, 0], ppr],
    [cmpa, [0, 1, 0], cmp],
    [vsla, [0, 0, 1], vsl]
] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n[0] IS NOT NULL
WITH n[0].id AS authorId, n[0].label AS authorName, "Literal" IN labels(n[0]) AS isLiteral, SUM(n[1][0]) AS paperCount, SUM(n[1][1]) AS comparisonCount, SUM(n[1][2]) AS visualizationCount
RETURN authorId, authorName, isLiteral, paperCount, comparisonCount, visualizationCount, (paperCount + comparisonCount + visualizationCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(:Problem:Thing {id: $ID})
OPTIONAL MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
WITH ppr, cmp, vsl
WHERE (ppr IS NULL OR ppr.created_at > $AFTER AND ppr.created_at < $BEFORE) AND (cmp IS NULL OR cmp.created_at > $AFTER AND cmp.created_at < $BEFORE) AND (vsl IS NULL OR vsl.created_at > $AFTER AND vsl.created_at < $BEFORE)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(ppra:Thing)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(cmpa:Thing)
OPTIONAL MATCH (vsl)-[:RELATED {predicate_id: 'hasAuthors'}]->(:List)-[:RELATED {predicate_id: "hasListElement"}]->(vsla:Thing)
WITH [ppra, cmpa, vsla] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL
RETURN COUNT(n)"""
    )
    fun findAllByResearchProblemId(id: ThingId, after: String, before: String, pageable: Pageable): Page<Neo4jAuthorRecord>
}

class Neo4jAuthorRecord(
    val authorId: ThingId,
    val authorName: String,
    val isLiteral: Boolean,
    val comparisonCount: Long,
    val paperCount: Long,
    val visualizationCount: Long,
    val totalCount: Long,
) {
    fun toAuthorRecord(): AuthorRecord =
        AuthorRecord(
            authorId.takeUnless { isLiteral },
            authorName,
            comparisonCount,
            paperCount,
            visualizationCount,
            totalCount,
        )
}
