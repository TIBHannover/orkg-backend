package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributorRecord
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

interface Neo4jContributorStatisticsRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query(
"""
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN n.created_by AS id, COUNT(n) AS paperCount, 0 AS comparisonCount, 0 AS contributionCount, 0 AS visualizationCount, 0 AS researchProblemCount
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN n.created_by AS id, 0 AS paperCount, COUNT(n) AS comparisonCount, 0 AS contributionCount, 0 AS visualizationCount, 0 AS researchProblemCount
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN n.created_by AS id, 0 AS paperCount, 0 AS comparisonCount, COUNT(n) AS contributionCount, 0 AS visualizationCount, 0 AS researchProblemCount
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN n.created_by AS id, 0 AS paperCount, 0 AS comparisonCount, 0 AS contributionCount, COUNT(n) AS visualizationCount, 0 AS researchProblemCount
    UNION ALL
    MATCH (n:Problem) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN n.created_by AS id, 0 AS paperCount, 0 AS comparisonCount, 0 AS contributionCount, 0 AS visualizationCount, COUNT(n) AS researchProblemCount
}
WITH id, SUM(paperCount) AS paperCount, SUM(contributionCount) AS contributionCount, SUM(comparisonCount) AS comparisonCount, SUM(visualizationCount) AS visualizationCount, SUM(researchProblemCount) AS researchProblemCount
RETURN id AS contributorId, paperCount, contributionCount, comparisonCount, visualizationCount, researchProblemCount, (paperCount + contributionCount + comparisonCount + visualizationCount + researchProblemCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
CALL () {
    MATCH (n:Paper)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:ComparisonPublished)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Contribution)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Visualization)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN DISTINCT n.created_by AS id
    UNION ALL
    MATCH (n:Problem)
    WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
    RETURN DISTINCT n.created_by AS id
}
WITH DISTINCT id
RETURN COUNT(id)"""
    )
    fun findAll(after: String, before: String, pageable: Pageable): Page<ContributorRecord>

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
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $AFTER AND n[0].created_at < $BEFORE
WITH n[0].created_by AS contributorId, SUM(n[1][0]) AS paperCount, SUM(n[1][1]) AS contributionCount, SUM(n[1][2]) AS comparisonCount, SUM(n[1][3]) AS visualizationCount, SUM(n[1][4]) AS researchProblemCount
RETURN contributorId, paperCount, contributionCount, comparisonCount, visualizationCount, researchProblemCount, (paperCount + contributionCount + comparisonCount + visualizationCount + researchProblemCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(r:ResearchField {id: $ID})
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
WITH DISTINCT n.created_by AS contributorId
RETURN COUNT(contributorId)"""
    )
    fun findAllByResearchFieldId(id: ThingId, after: String, before: String, pageable: Pageable): Page<ContributorRecord>

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
}
WITH field
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
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $AFTER AND n[0].created_at < $BEFORE
WITH n[0].created_by AS contributorId, SUM(n[1][0]) AS paperCount, SUM(n[1][1]) AS contributionCount, SUM(n[1][2]) AS comparisonCount, SUM(n[1][3]) AS visualizationCount, SUM(n[1][4]) AS researchProblemCount
RETURN contributorId, paperCount, contributionCount, comparisonCount, visualizationCount, researchProblemCount, (paperCount + contributionCount + comparisonCount + visualizationCount + researchProblemCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery =
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
}
WITH field
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P30"}]->(field)
OPTIONAL MATCH (ppr)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
OPTIONAL MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
WITH DISTINCT n.created_by AS contributorId
RETURN COUNT(contributorId)"""
    )
    fun findAllByResearchFieldIdIncludingSubfields(id: ThingId, after: String, before: String, pageable: Pageable): Page<ContributorRecord>

    @Query(
"""
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem {id: $ID})
WITH [
    [ppr, [1, 0, 0, 0, 0]],
    [ctr, [0, 1, 0, 0, 0]],
    [cmp, [0, 0, 1, 0, 0]],
    [vsl, [0, 0, 0, 1, 0]],
    [prb, [0, 0, 0, 0, 1]]
] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $AFTER AND n[0].created_at < $BEFORE
WITH n[0].created_by AS contributorId, SUM(n[1][0]) AS paperCount, SUM(n[1][1]) AS contributionCount, SUM(n[1][2]) AS comparisonCount, SUM(n[1][3]) AS visualizationCount, SUM(n[1][4]) AS researchProblemCount
RETURN contributorId, paperCount, contributionCount, comparisonCount, visualizationCount, researchProblemCount, (paperCount + contributionCount + comparisonCount + visualizationCount + researchProblemCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery = """
MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
MATCH (ctr)-[:RELATED {predicate_id: "P32"}]->(prb:Problem {id: $ID})
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
WITH DISTINCT n.created_by AS contributorId
RETURN COUNT(contributorId)"""
    )
    fun findAllByResearchProblemId(id: ThingId, after: String, before: String, pageable: Pageable): Page<ContributorRecord>

    @Query(
"""
CALL () {
    MATCH (problem:Problem {id: $ID})
    RETURN problem
    UNION ALL
    MATCH (problem:Problem {id: $ID})
    CALL custom.subgraph(problem, {labelFilter: "+Problem", relationshipFilter: "<RELATED"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "subProblem"
    RETURN startNode(rel) AS problem
}
WITH problem AS prb
MATCH (ctr:Contribution)-[:RELATED {predicate_id: "P32"}]->(prb)
OPTIONAL MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
WITH [
    [ppr, [1, 0, 0, 0, 0]],
    [ctr, [0, 1, 0, 0, 0]],
    [cmp, [0, 0, 1, 0, 0]],
    [vsl, [0, 0, 0, 1, 0]],
    [prb, [0, 0, 0, 0, 1]]
] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n[0] IS NOT NULL AND n[0].created_by <> "00000000-0000-0000-0000-000000000000" AND n[0].created_at > $AFTER AND n[0].created_at < $BEFORE
WITH n[0].created_by AS contributorId, SUM(n[1][0]) AS paperCount, SUM(n[1][1]) AS contributionCount, SUM(n[1][2]) AS comparisonCount, SUM(n[1][3]) AS visualizationCount, SUM(n[1][4]) AS researchProblemCount
RETURN contributorId, paperCount, contributionCount, comparisonCount, visualizationCount, researchProblemCount, (paperCount + contributionCount + comparisonCount + visualizationCount + researchProblemCount) AS totalCount $ORDER_BY_PAGE_PARAMS""",
        countQuery =
"""
CALL () {
    MATCH (problem:Problem {id: $ID})
    RETURN problem
    UNION ALL
    MATCH (problem:Problem {id: $ID})
    CALL custom.subgraph(problem, {labelFilter: "+Problem", relationshipFilter: "<RELATED"})
    YIELD relationships
    UNWIND relationships AS rel
    WITH rel
    WHERE rel.predicate_id = "subProblem"
    RETURN startNode(rel) AS problem
}
WITH problem AS prb
MATCH (ctr:Contribution)-[:RELATED {predicate_id: "P32"}]->(prb)
OPTIONAL MATCH (ppr:Paper)-[:RELATED {predicate_id: "P31"}]->(ctr)
OPTIONAL MATCH (cmp:ComparisonPublished)-[:RELATED {predicate_id: "compareContribution"}]->(ctr)
OPTIONAL MATCH (cmp)-[:RELATED {predicate_id: "hasVisualization"}]->(vsl:Visualization)
WITH [ppr, ctr, cmp, vsl, prb] AS nodes
UNWIND nodes AS n
WITH DISTINCT n
WHERE n IS NOT NULL AND n.created_by <> "00000000-0000-0000-0000-000000000000" AND n.created_at > $AFTER AND n.created_at < $BEFORE
WITH DISTINCT n.created_by AS contributorId
RETURN COUNT(contributorId)"""
    )
    fun findAllByResearchProblemIdIncludingSubProblems(id: ThingId, after: String, before: String, pageable: Pageable): Page<ContributorRecord>
}
