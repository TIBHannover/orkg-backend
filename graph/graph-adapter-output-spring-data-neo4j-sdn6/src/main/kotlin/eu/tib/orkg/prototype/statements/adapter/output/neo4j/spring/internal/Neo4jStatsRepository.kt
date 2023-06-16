package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ChangeLogResponse
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val date = "${'$'}date"
private const val id = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jStatsRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""CALL apoc.meta.stats()""")
    fun getGraphMetaData(): Iterable<HashMap<String, Any>>

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
RETURN observatoryId, papers, comparisons, total $PAGE_PARAMS""",
    countQuery = """
MATCH (n:Paper:Resource)
WHERE n.observatory_id <> '00000000-0000-0000-0000-000000000000'
RETURN COUNT(DISTINCT(n.observatory_id))""")
    fun getObservatoriesPapersAndComparisonsCount(pageable: Pageable): Page<ObservatoryStats>

    @Query("""
CALL {
    MATCH (n:Paper:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN n.created_by AS id, COUNT(n) AS contributions
    UNION ALL
    MATCH (n:Comparison:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN n.created_by AS id, COUNT(n) AS contributions
    UNION ALL
    MATCH (n:Contribution:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN n.created_by AS id, COUNT(n) AS contributions
    UNION ALL
    MATCH (n:Visualization:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN n.created_by AS id, COUNT(n) AS contributions
    UNION ALL
    MATCH (n:Problem:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN n.created_by AS id, COUNT(n) AS contributions
} WITH id, contributions
RETURN id, SUM(contributions) AS contributions $PAGE_PARAMS""",
        countQuery = """
CALL {
    MATCH (n:Paper:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Comparison:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Contribution:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Visualization:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Problem:Resource) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $date RETURN DISTINCT n.created_by AS id
} WITH DISTINCT id
RETURN COUNT(id)""")
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<Neo4jContributorRecord>

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
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $PAGE_PARAMS""",
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
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ThingId, date: String, pageable: Pageable): Page<Neo4jContributorRecord>

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
RETURN contributor, papers, contributions, comparisons, visualizations, problems, (papers + contributions + comparisons + visualizations + problems) AS total $PAGE_PARAMS""",
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
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ThingId, date: String, pageable: Pageable): Page<Neo4jContributorRecord>

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
RETURN sub.id AS id, sub.label AS label, sub.created_at AS createdAt, COALESCE(sub.created_by, '00000000-0000-0000-0000-000000000000') AS createdBy, labels(sub) AS classes ORDER BY createdAt DESC $PAGE_PARAMS""",
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
    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>

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
RETURN n.id AS id, n.label AS label, n.created_at AS createdAt, COALESCE(n.created_by, '00000000-0000-0000-0000-000000000000') AS createdBy, labels(n) AS classes $PAGE_PARAMS""",
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
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (paper: Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem:Resource) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount $PAGE_PARAMS""",
        countQuery = "MATCH (paper: Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem:Resource) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    @Query("""MATCH (n:Thing) WHERE NOT (n)--() RETURN COUNT(n) AS orphanedNodes""")
    fun getOrphanedNodesCount(): Long
}

data class Neo4jContributorRecord(
    val contributor: String, // TODO: Should be ContributorId
    val comparisons: Long,
    val papers: Long,
    val contributions: Long,
    val problems: Long,
    val visualizations: Long,
    val total: Long
)
