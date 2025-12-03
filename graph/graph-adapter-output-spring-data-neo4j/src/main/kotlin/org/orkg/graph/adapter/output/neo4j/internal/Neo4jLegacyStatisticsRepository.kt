package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.LegacyContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val DATE = "${'$'}date"
private const val ID = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

interface Neo4jLegacyStatisticsRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query(
        """
CALL () {
    MATCH (n:Paper)
    WHERE n.observatory_id <> '00000000-0000-0000-0000-000000000000'
    RETURN n.observatory_id AS observatoryId, COUNT(n) AS papers, 0 AS comparisons
    UNION
    MATCH (c:ComparisonPublished:LatestVersion)
    WHERE c.observatory_id <> '00000000-0000-0000-0000-000000000000'
    RETURN c.observatory_id AS observatoryId, 0 AS papers, COUNT(c) AS comparisons
}
WITH observatoryId, SUM(comparisons) AS comparisons, SUM(papers) AS papers
WITH observatoryId, comparisons, papers, comparisons + papers AS total
ORDER BY total DESC
RETURN observatoryId, papers, comparisons, total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (n:Paper)
    WHERE n.observatory_id <> '00000000-0000-0000-0000-000000000000'
    RETURN n.observatory_id AS observatoryId, COUNT(n) AS papers, 0 AS comparisons
    UNION
    MATCH (c:ComparisonPublished:LatestVersion)
    WHERE c.observatory_id <> '00000000-0000-0000-0000-000000000000'
    RETURN c.observatory_id AS observatoryId, 0 AS papers, COUNT(c) AS comparisons
}
RETURN COUNT(DISTINCT observatoryId)"""
    )
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    @Query(
        """
CALL () {
    MATCH (n:Paper {observatory_id: $ID})
    RETURN COUNT(n) AS papers
}
CALL () {
    MATCH (n:ComparisonPublished:LatestVersion {observatory_id: $ID})
    RETURN COUNT(n) AS comparisons
}
RETURN $ID AS observatoryId, comparisons, papers, comparisons + papers AS total"""
    )
    fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats>

    @Query(
        """
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN n.created_by AS id, COUNT(n) AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN n.created_by AS id, 0 AS papers, COUNT(n) AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, COUNT(n) AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, COUNT(n) AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Problem) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, COUNT(n) AS problems
} WITH id, SUM(papers) AS papers, SUM(contributions) AS contributions, SUM(comparisons) AS comparisons, SUM(visualizations) AS visualizations, SUM(problems) AS problems
RETURN id AS contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Problem)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
    RETURN DISTINCT n.created_by AS id
} WITH DISTINCT id
RETURN COUNT(id)"""
    )
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<LegacyContributorRecord>

    /**
     * This query fetches the contributor IDs from sub research fields as well.
     */
    @Query(
        """
CALL () {
    MATCH (field:ResearchField {id: $ID})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $ID})
    CALL custom.subgraph(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [
    [ppr, [1, 0, 0, 0, 0]],
    [ctr, [0, 1, 0, 0, 0]],
    [cmp, [0, 0, 1, 0, 0]],
    [vsl, [0, 0, 0, 1, 0]],
    [prb, [0, 0, 0, 0, 1]]
] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $DATE
WITH n[0].created_by AS contributor, SUM(n[1][0]) AS papers, SUM(n[1][1]) AS contributions, SUM(n[1][2]) AS comparisons, SUM(n[1][3]) AS visualizations, SUM(n[1][4]) AS problems
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (field:ResearchField {id: $ID})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $ID})
    CALL custom.subgraph(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)"""
    )
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ThingId, date: String, pageable: Pageable): Page<LegacyContributorRecord>

    /**
     * This query fetches the contributor ID from only research fields and excludes sub research fields.
     */
    @Query(
        """
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(:ResearchField {id: $ID})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [
    [ppr, [1, 0, 0, 0, 0]],
    [ctr, [0, 1, 0, 0, 0]],
    [cmp, [0, 0, 1, 0, 0]],
    [vsl, [0, 0, 0, 1, 0]],
    [prb, [0, 0, 0, 0, 1]]
] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $DATE
WITH n[0].created_by AS contributor, SUM(n[1][0]) AS papers, SUM(n[1][1]) AS contributions, SUM(n[1][2]) AS comparisons, SUM(n[1][3]) AS visualizations, SUM(n[1][4]) AS problems
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(r:ResearchField {id: $ID})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $DATE
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)"""
    )
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ThingId, date: String, pageable: Pageable): Page<LegacyContributorRecord>
}
