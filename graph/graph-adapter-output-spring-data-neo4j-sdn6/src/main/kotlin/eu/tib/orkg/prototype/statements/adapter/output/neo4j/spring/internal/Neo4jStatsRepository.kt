package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val date = "${'$'}date"
private const val id = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} $PAGE_PARAMS"

interface Neo4jStatsRepository : Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""MATCH (n:ResearchField:Resource) WITH n OPTIONAL MATCH (n)-[:RELATED*0..3 {predicate_id: 'P36'}]->(r:ResearchField:Resource) OPTIONAL MATCH (r)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper:Resource) RETURN n.id AS fieldId, n.label AS field, COUNT(p) AS papers""")
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    @Query("""MATCH (n:Paper:Resource {observatory_id: $id}) RETURN COUNT(n) As totalPapers""")
    fun getObservatoryPapersCount(id: ObservatoryId): Long

    @Query("""MATCH (n:Comparison:Resource {observatory_id: $id}) RETURN COUNT(n) As totalComparisons""")
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    @Query("""
MATCH (n:Paper:Resource)
WHERE n.observatory_id <> '00000000-0000-0000-0000-000000000000'
WITH n.observatory_id AS observatoryId, COUNT(n) AS papers
OPTIONAL MATCH (c:Comparison:Resource)
WHERE c.observatory_id <> '00000000-0000-0000-0000-000000000000' AND c.observatory_id = observatoryId
WITH observatoryId, COUNT(c) AS comparisons, papers
WITH observatoryId, comparisons, papers, comparisons + papers AS total
ORDER BY total DESC
RETURN observatoryId, papers, comparisons, total $ORDER_BY_PAGE_PARAMS""",
    countQuery = """
MATCH (n:Paper:Resource)
WHERE n.observatory_id <> '00000000-0000-0000-0000-000000000000'
RETURN COUNT(DISTINCT(n.observatory_id))""")
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    @Query("""
MATCH (rsf:ResearchField:Resource {id: $id})
OPTIONAL MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(rsf)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(:Contribution:Resource)<-[:RELATED {predicate_id: "compareContribution"}]-(cmp:Comparison:Resource)
WITH rsf.id AS id, COUNT(DISTINCT ppr) AS papers, COUNT(DISTINCT cmp) AS comparisons
RETURN id, papers, comparisons, (papers + comparisons) AS total""")
    fun findResearchFieldStatsById(id: ThingId): Optional<ResearchFieldStats>

    @Query("""
MATCH (rsf:ResearchField:Resource)<-[:RELATED*0.. {predicate_id: "P36"}]-(:ResearchField:Resource {id: $id})
OPTIONAL MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(rsf)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(:Contribution:Resource)<-[:RELATED {predicate_id: "compareContribution"}]-(cmp:Comparison:Resource)
WITH $id AS id, COUNT(DISTINCT ppr) AS papers, COUNT(DISTINCT cmp) AS comparisons
RETURN id, papers, comparisons, (papers + comparisons) AS total""")
    fun findResearchFieldStatsByIdIncludingSubfields(id: ThingId): Optional<ResearchFieldStats>

    @Query("""
OPTIONAL MATCH (n:Paper:Resource {observatory_id: $id})
WITH n.observatory_id AS observatoryId, COUNT(n) AS papers
OPTIONAL MATCH (c:Comparison:Resource {observatory_id: $id})
WITH observatoryId, COUNT(c) AS comparisons, papers
WITH observatoryId, comparisons, papers, comparisons + papers AS total
ORDER BY total DESC
RETURN $id AS observatoryId, papers, comparisons, total""")
    fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats>

    @Query("""
CALL {
    MATCH (n:Paper:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, COUNT(n) AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Comparison:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, COUNT(n) AS comparisons, 0 AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Contribution:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, COUNT(n) AS contributions, 0 AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Visualization:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, COUNT(n) AS visualizations, 0 AS problems
    UNION ALL
    MATCH (n:Problem:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN n.created_by AS id, 0 AS papers, 0 AS comparisons, 0 AS contributions, 0 AS visualizations, COUNT(n) AS problems
} WITH id, SUM(papers) AS papers, SUM(contributions) AS contributions, SUM(comparisons) AS comparisons, SUM(visualizations) AS visualizations, SUM(problems) AS problems
RETURN id AS contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL {
    MATCH (n:Paper:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Comparison:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Contribution:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Visualization:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Problem:Resource)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
    RETURN DISTINCT n.created_by AS id
} WITH DISTINCT id
RETURN COUNT(id)""")
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<RetrieveStatisticsUseCase.ContributorRecord>

    /**
     * This query fetches the contributor IDs from sub research fields as well.
     */
    @Query("""
CALL {
    MATCH (field:ResearchField:Resource {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField:Resource {id: $id})
    CALL apoc.path.subgraphAll(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution:Resource)
OPTIONAL MATCH (cmp:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization:Resource)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem:Resource)
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
CALL {
    MATCH (field:ResearchField:Resource {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField:Resource {id: $id})
    CALL apoc.path.subgraphAll(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution:Resource)
OPTIONAL MATCH (cmp:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization:Resource)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem:Resource)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)""")
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ThingId, date: String, pageable: Pageable): Page<RetrieveStatisticsUseCase.ContributorRecord>

    /**
     * This query fetches the contributor ID from only research fields and excludes sub research fields.
     */
    @Query("""
MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(:ResearchField:Resource {id: $id})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution:Resource)
OPTIONAL MATCH (cmp:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization:Resource)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem:Resource)
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
MATCH (ppr:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(r:ResearchField:Resource {id: $id})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution:Resource)
OPTIONAL MATCH (cmp:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization:Resource)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem:Resource)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date
WITH DISTINCT n.created_by AS contributor
RETURN COUNT(contributor)""")
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ThingId, date: String, pageable: Pageable): Page<RetrieveStatisticsUseCase.ContributorRecord>

    @Query("""
CALL {
    MATCH (sub:Paper:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'PaperDeleted' IN labels RETURN sub
    UNION ALL
    MATCH (sub:Contribution:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'ContributionDeleted' IN labels RETURN sub
    UNION ALL
    MATCH (sub:Problem:Resource) RETURN sub
    UNION ALL
    MATCH (sub:Visualization:Resource) RETURN sub
    UNION ALL
    MATCH (sub:Comparison:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'ComparisonDeleted' IN labels RETURN sub
} WITH sub
ORDER BY sub.created_at DESC
RETURN sub $PAGE_PARAMS""",
        countQuery = """
CALL {
    MATCH (sub:Paper:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'PaperDeleted' IN labels RETURN sub
    UNION ALL
    MATCH (sub:Contribution:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'ContributionDeleted' IN labels RETURN sub
    UNION ALL
    MATCH (sub:Problem:Resource) RETURN sub
    UNION ALL
    MATCH (sub:Visualization:Resource) RETURN sub
    UNION ALL
    MATCH (sub:Comparison:Resource) WITH labels(sub) AS labels, sub WHERE NOT 'ComparisonDeleted' IN labels RETURN sub
} WITH sub
RETURN COUNT(sub)
""")
    fun getChangeLog(pageable: Pageable): Page<Neo4jResource>

    @Query("""
CALL {
    MATCH (field:ResearchField:Resource {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField:Resource {id: $id})
    CALL apoc.path.subgraphAll(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (p:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (c:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(:Contribution:Resource)<-[:RELATED {predicate_id:"P31"}]-(p)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "hasVisualization"}]->(v:Visualization:Resource)
WITH [p, c, v] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL
WITH n
ORDER BY n.created_at DESC
RETURN n $PAGE_PARAMS""",
        countQuery = """
CALL {
    MATCH (field:ResearchField:Resource {id: $id})
    RETURN field
    UNION ALL
    MATCH (field:ResearchField:Resource {id: $id})
    CALL apoc.path.subgraphAll(field, {labelFilter: "+ResearchField", relationshipFilter: "RELATED>"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "P36"
    RETURN endNode(rel) AS field
} WITH field
MATCH (p:Paper:Resource)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (c:Comparison:Resource)-[:RELATED {predicate_id: "compareContribution"}]->(:Contribution:Resource)<-[:RELATED {predicate_id:"P31"}]-(p)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "hasVisualization"}]->(v:Visualization:Resource)
WITH [p, c, v] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL
RETURN COUNT(n)""")
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (paper: Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem:Resource) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = "MATCH (paper: Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem:Resource) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    @Query("""MATCH (n:Thing) WHERE NOT (n)--() RETURN COUNT(n) AS orphanedNodes""")
    fun getOrphanedNodesCount(): Long
}
