package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ChangeLogResponse
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryResources
import eu.tib.orkg.prototype.statements.spi.ResultObject
import eu.tib.orkg.prototype.statements.spi.TopContributorIdentifiers
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

    @Query("""MATCH (n:ResearchField) WITH n OPTIONAL MATCH (n)-[:RELATED*0..3 {predicate_id: 'P36'}]->(r:ResearchField) OPTIONAL MATCH (r)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper) RETURN n.resource_id AS fieldId, n.label AS field, COUNT(p) AS papers""")
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    @Query("""MATCH (n:Paper {observatory_id: $id}) RETURN COUNT(n) As totalPapers""")
    fun getObservatoryPapersCount(id: ObservatoryId): Long

    @Query("""MATCH (n:Comparison {observatory_id: $id}) RETURN COUNT(n) As totalComparisons""")
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    @Query("""MATCH (n:Paper) WHERE n.observatory_id<>'00000000-0000-0000-0000-000000000000' WITH DISTINCT (n.observatory_id) AS observatoryId, COUNT(n) AS resources OPTIONAL MATCH (c:Comparison) where c.observatory_id<>'00000000-0000-0000-0000-000000000000' AND c.observatory_id = observatoryId WITH DISTINCT (c.observatory_id) as cobservatoryId, count(c) as comparisons, resources, observatoryId RETURN observatoryId, resources, comparisons""")
    fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources>

    @Query("""MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'Problem' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > $date ) RETURN DISTINCT sub.created_by AS id, count(sub) AS contributions $PAGE_PARAMS""",
        countQuery = "MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'Problem' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > $date ) RETURN DISTINCT COUNT(sub.created_by) AS cnt")
    fun getTopCurrentContributorIdsAndContributionsCount(date: String, pageable: Pageable): Page<TopContributorIdentifiers>

    /**
     * This query fetches the contributor IDs from sub research fields as well.
     */
    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND c.created_by IS NOT NULL AND c.created_by <> '00000000-0000-0000-0000-000000000000' AND c.created_at > $date  WITH  c.created_by AS contribution_creators, COUNT(c.created_by) AS cnt RETURN COLLECT({id:contribution_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND comparison1.created_by IS NOT NULL AND comparison1.created_by <> '00000000-0000-0000-0000-000000000000' AND comparison1.created_at > $date WITH  comparison1.created_by AS comparison_creators, COUNT(comparison1.created_by) AS cnt RETURN COLLECT({id:comparison_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND p1.created_by IS NOT NULL AND p1.created_by <> '00000000-0000-0000-0000-000000000000' AND p1.created_at > $date WITH  p1.created_by AS paper_creators, COUNT(p1.created_by) AS cnt RETURN COLLECT({id:paper_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH (v:Visualization)<-[:RELATED]-(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND v.created_by IS NOT NULL AND v.created_by <> '00000000-0000-0000-0000-000000000000' AND v.created_at > $date WITH  v.created_by AS visualization_creators, COUNT(v.created_by) AS cnt RETURN COLLECT({id:visualization_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH (problem:Problem)<-[:RELATED]-(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND problem.created_by IS NOT NULL AND problem.created_by <> '00000000-0000-0000-0000-000000000000' AND problem.created_at > $date WITH  problem.created_by AS problem_creators, COUNT(problem.created_by) AS cnt RETURN COLLECT({id:problem_creators, cnt:cnt}) AS total""")
    fun getTopCurContribIdsAndContribCountByResearchFieldId(id: ResourceId, date: String): List<List<Map<String, List<ResultObject>>>>

    /**
     * This query fetches the contributor ID from only research fields and excludes sub research fields.
     */
    @Query("""MATCH (research:ResearchField{resource_id: $id}) WITH COLLECT(research) AS r OPTIONAL MATCH(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND c.created_by IS NOT NULL AND c.created_by <> '00000000-0000-0000-0000-000000000000' AND c.created_at > $date  WITH c.created_by AS contribution_creators, COUNT(c.created_by) AS cnt RETURN COLLECT({id:contribution_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField{resource_id: $id})WITH COLLECT(research) AS r OPTIONAL MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND comparison1.created_by IS NOT NULL AND comparison1.created_by <> '00000000-0000-0000-0000-000000000000' AND comparison1.created_at > $date WITH  comparison1.created_by AS comparison_creators,COUNT(comparison1.created_by) AS cnt RETURN COLLECT({id:comparison_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField{resource_id: $id}) WITH COLLECT(research) AS r OPTIONAL MATCH(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND p1.created_by IS NOT NULL AND p1.created_by <> '00000000-0000-0000-0000-000000000000' AND p1.created_at > $date WITH  p1.created_by AS paper_creators, COUNT(p1.created_by) AS cnt RETURN COLLECT({id:paper_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField{resource_id: $id}) WITH COLLECT(research) AS r OPTIONAL MATCH (v:Visualization)<-[:RELATED]-(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND v.created_by IS NOT NULL AND v.created_by <> '00000000-0000-0000-0000-000000000000' AND v.created_at > $date WITH  v.created_by AS visualization_creators, COUNT(v.created_by) AS cnt RETURN COLLECT({id:visualization_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField{resource_id: $id}) WITH COLLECT(research) AS r OPTIONAL MATCH (problem:Problem)<-[:RELATED]-(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND problem.created_by IS NOT NULL AND problem.created_by <> '00000000-0000-0000-0000-000000000000'AND problem.created_at > $date WITH  problem.created_by AS problem_creators, COUNT(problem.created_by) AS cnt RETURN COLLECT({id:problem_creators, cnt:cnt}) AS total""")
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id: ResourceId, date: String): List<List<Map<String, List<ResultObject>>>>

    @Query("""MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (NOT 'PaperDeleted' IN  labels(sub)) AND (NOT 'ContributionDeleted' IN labels(sub)) RETURN sub.resource_id AS id, sub.label AS label, sub.created_at AS createdAt, COALESCE(sub.created_by, '00000000-0000-0000-0000-000000000000') as createdBy, labels(sub) AS classes ORDER BY createdAt DESC $PAGE_PARAMS""",
        countQuery = "MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (NOT 'PaperDeleted' IN  labels(sub)) AND (NOT 'ContributionDeleted' IN labels(sub)) RETURN count(sub.created_by) as cnt")
    fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (comp:Comparison)-[:RELATED* 0..4]->(contribution:Contribution)<-[:RELATED* 0..4]-(p:Paper)-[:RELATED* 0..4]->(r:ResearchField{resource_id: $id}) OPTIONAL MATCH(v:Visualization)<-[:RELATED* 0..4]-(comp) WITH COLLECT([v.resource_id,v.label, v.created_at, v.created_by,labels(v)])  +  COLLECT([comp.resource_id,comp.label, comp.created_at, comp.created_by, labels(comp)]) + COLLECT([contribution.resource_id,contribution.label, contribution.created_at, contribution.created_by, labels(contribution)]) + COLLECT([p.resource_id,p.label, p.created_at, p.created_by, labels(p)]) + COLLECT([r.resource_id,r.label, r.created_at, r.created_by, labels(r)]) AS items UNWIND items AS changelogs  WITH DISTINCT changelogs[0] AS id, changelogs[1] AS label, changelogs[2] AS createdAt, changelogs[3] AS createdBy, changelogs[4] AS classes WHERE id IS NOT NULL RETURN id, label, createdAt, createdBy, classes $PAGE_PARAMS""",
        countQuery = "MATCH (comp:Comparison)-[:RELATED* 0..4]->(contribution:Contribution)<-[:RELATED* 0..4]-(p:Paper)-[:RELATED* 0..4]->(r:ResearchField{resource_id: $id}) OPTIONAL MATCH(v:Visualization)<-[:RELATED* 0..4]-(comp) WITH COLLECT([v.resource_id,v.label, v.created_at, v.created_by,labels(v)]) + COLLECT([comp.resource_id,comp.label, comp.created_at, comp.created_by, labels(comp)]) + COLLECT([contribution.resource_id,contribution.label, contribution.created_at, contribution.created_by, labels(contribution)]) + COLLECT([p.resource_id,p.label, p.created_at, p.created_by, labels(p)]) + COLLECT([r.resource_id,r.label, r.created_at, r.created_by, labels(r)]) AS items UNWIND items AS changelogs WITH DISTINCT changelogs WHERE changelogs[0] IS NOT NULL RETURN COUNT(changelogs) AS cnt")
    fun getChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLogResponse>

    @Query("""MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN id, researchProblem, papersCount $PAGE_PARAMS""",
        countQuery = "MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor RETURN count(researchProblem) as cnt")
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>

    @Query("""MATCH (n:Thing) WHERE NOT (n)--() RETURN COUNT(n) AS orphanedNodes""")
    fun getOrphanedNodesCount(): Long
}
