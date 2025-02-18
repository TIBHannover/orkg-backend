package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.FieldsStats
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.domain.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val date = "${'$'}date"
private const val id = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

interface Neo4jLegacyStatisticsRepository : Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""MATCH (n:ResearchField) RETURN n.id AS fieldId, n.label AS field, COUNT { MATCH (n)-[:RELATED*0..3 {predicate_id: 'P36'}]->(r:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper) RETURN p } AS papers""")
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    @Query("""MATCH (n:Paper {observatory_id: $id}) RETURN COUNT(n) As totalPapers""")
    fun getObservatoryPapersCount(id: ObservatoryId): Long

    @Query("""MATCH (n:ComparisonPublished:LatestVersion {observatory_id: $id}) RETURN COUNT(n) As totalComparisons""")
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    @Query("""
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
RETURN COUNT(DISTINCT observatoryId)""")
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    @Query("""
MATCH (rsf:ResearchField {id: $id})
CALL (rsf) {
    MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(rsf)
    RETURN COUNT(DISTINCT ppr) as papers
}
WITH rsf, papers
CALL (rsf) {
    MATCH (rsf)<-[:RELATED {predicate_id: "hasSubject"}]-(cmp:ComparisonPublished:LatestVersion)
    RETURN COUNT(DISTINCT cmp) AS comparisons
}
WITH papers, comparisons
RETURN $id AS id, papers, comparisons, (papers + comparisons) AS total""")
    fun findResearchFieldStatsById(id: ThingId): Optional<ResearchFieldStats>

    @Query("""
CALL () {
    MATCH (field:ResearchField {id: $id})
    RETURN field AS rsf
    UNION ALL
    MATCH (field:ResearchField {id: $id})
    CALL custom.subgraph(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS rsf
}
WITH COLLECT(rsf) AS rsfs
CALL (rsfs) {
    UNWIND rsfs AS rsf
    MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(rsf)
    RETURN COUNT(DISTINCT ppr) as papers
}
WITH rsfs, papers
CALL (rsfs) {
    UNWIND rsfs AS rsf
    MATCH (rsf)<-[:RELATED {predicate_id: "hasSubject"}]-(cmp:ComparisonPublished:LatestVersion)
    RETURN COUNT(DISTINCT cmp) AS comparisons
}
WITH papers, comparisons
RETURN $id AS id, papers, comparisons, (papers + comparisons) AS total""")
    fun findResearchFieldStatsByIdIncludingSubfields(id: ThingId): Optional<ResearchFieldStats>

    @Query("""
CALL () {
    MATCH (n:Paper {observatory_id: $id})
    RETURN COUNT(n) AS papers
}
CALL () {
    MATCH (n:ComparisonPublished:LatestVersion {observatory_id: $id})
    RETURN COUNT(n) AS comparisons
}
RETURN $id AS observatoryId, comparisons, papers, comparisons + papers AS total""")
    fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats>

    @Query("""
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, COUNT(n) AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, COUNT(n) AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, COUNT(n) AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, COUNT(n) AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Problem) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, COUNT(n) AS problems
} WITH id, SUM(papers) AS papers, SUM(contributions) AS contributions, SUM(comparisons) AS comparisons, SUM(visualizations) AS visualizations, SUM(problems) AS problems
RETURN id AS contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Problem)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
} WITH DISTINCT id
RETURN COUNT(id)""")
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<ContributorRecord>

    /**
     * This query fetches the contributor IDs from sub research fields as well.
     */
    @Query("""
CALL () {
    MATCH (field:ResearchField {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $id})
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
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $date
WITH n[0].created_by AS contributor, SUM(n[1][0]) AS papers, SUM(n[1][1]) AS contributions, SUM(n[1][2]) AS comparisons, SUM(n[1][3]) AS visualizations, SUM(n[1][4]) AS problems
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
    countQuery = """
CALL () {
    MATCH (field:ResearchField {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $id})
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
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)""")
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ThingId, date: String, pageable: Pageable): Page<ContributorRecord>

    /**
     * This query fetches the contributor ID from only research fields and excludes sub research fields.
     */
    @Query("""
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(:ResearchField {id: $id})
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
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $date
WITH n[0].created_by AS contributor, SUM(n[1][0]) AS papers, SUM(n[1][1]) AS contributions, SUM(n[1][2]) AS comparisons, SUM(n[1][3]) AS visualizations, SUM(n[1][4]) AS problems
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(r:ResearchField {id: $id})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)""")
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ThingId, date: String, pageable: Pageable): Page<ContributorRecord>

    @Query("""
CALL () {
    MATCH (n:Paper) RETURN n
    UNION ALL
    MATCH (n:Contribution) RETURN n
    UNION ALL
    MATCH (n:Problem) RETURN n
    UNION ALL
    MATCH (n:Visualization) RETURN n
    UNION ALL
    MATCH (n:ComparisonPublished) RETURN n
}
RETURN n $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (n:Paper) RETURN n
    UNION ALL
    MATCH (n:Contribution) RETURN n
    UNION ALL
    MATCH (n:Problem) RETURN n
    UNION ALL
    MATCH (n:Visualization) RETURN n
    UNION ALL
    MATCH (n:ComparisonPublished) RETURN n
}
RETURN COUNT(n)
""")
    fun getChangeLog(pageable: Pageable): Page<Neo4jResource>

    @Query("""
CALL () {
    MATCH (field:ResearchField {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $id})
    CALL custom.subgraph(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (p:Paper)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (c:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(:Contribution)<-[:RELATED {predicate_id:"P31"}]-(p)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "hasVisualization"}]->(v:Visualization)
WITH [p, c, v] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL
RETURN n $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (field:ResearchField {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField {id: $id})
    CALL custom.subgraph(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (p:Paper)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (c:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(:Contribution)<-[:RELATED {predicate_id:"P31"}]-(p)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "hasVisualization"}]->(v:Visualization)
WITH [p, c, v] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL
RETURN COUNT(n)""")
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = "MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    @Query("""MATCH (n:Thing) WHERE NOT (n)--() RETURN COUNT(n) AS orphanedNodes""")
    fun getOrphanedNodesCount(): Long
}
