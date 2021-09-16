package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.toCypher
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.ports.ChangeLogResponse
import eu.tib.orkg.prototype.statements.ports.FieldsStats
import eu.tib.orkg.prototype.statements.ports.ObservatoryResources
import eu.tib.orkg.prototype.statements.ports.ResultObject
import eu.tib.orkg.prototype.statements.ports.StatsRepository
import eu.tib.orkg.prototype.statements.ports.TopContributorIdentifiers
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import eu.tib.orkg.prototype.statements.ports.TrendingResearchProblems
import org.neo4j.driver.Record
import org.neo4j.driver.types.TypeSystem


@Component
class StatsPersistenceAdapter(
    private val client: Neo4jClient
): StatsRepository {
    //Adding an unchecked cast due to the direct addition of SDN Mapper to query
    @Suppress("UNCHECKED_CAST")
    override fun getGraphMetaData(): Iterable<Map<String, Any?>> {
        val query =  """CALL apoc.meta.stats()"""
        val result =  client
            .query(query)
            .fetchAs(Map::class.java)
            .mappedBy(statisticsDataMapper)
            .all()
        return result as Iterable<Map<String, Any?>>
    }

    override fun getResearchFieldsCount(): Long {
        val query =  """
           MATCH (n:Resource{resource_id: 'R11'}) CALL apoc.path.subgraphAll(n, {relationshipFilter: '>'}) YIELD nodes UNWIND nodes as field WITH COUNT(field) as cnt RETURN cnt
       """.trimIndent()
        return client
            .query(query)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun getResearchFieldsPapersCount(): Iterable<FieldsStats> {
        val query =  """
           MATCH (n:ResearchField) WITH n OPTIONAL MATCH (n)-[:RELATED*0..3 {predicate_id: 'P36'}]->(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper) RETURN n.resource_id AS fieldId, n.label AS field, COUNT(p) AS papers
       """.trimIndent()

        val result = client
            .query(query)
            .fetchAs(FieldsStats::class.java)
            .mappedBy(fsDataMapper)
            .all()

        return result.map { toFieldStats(it) }
    }

    override fun getObservatoryPapersCount(id: ObservatoryId): Long{
       val query =  """
           MATCH (n:Paper) WHERE n.`observatory_id` = '$id' RETURN COUNT(n)
       """.trimIndent()
        return client
            .query(query)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long {
        val query =  """
           MATCH (n:Comparison) WHERE n.observatory_id = '$id' RETURN COUNT(n)
       """.trimIndent()
        return client
            .query(query)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun getObservatoriesPapersAndComparisonsCount(): Iterable<ObservatoryResources> {
        val query =  """MATCH (n:Paper) WHERE n.observatory_id<>'00000000-0000-0000-0000-000000000000' WITH DISTINCT (n.observatory_id) AS observatoryId, COUNT(n) AS resources OPTIONAL MATCH (c:Comparison) where c.observatory_id<>'00000000-0000-0000-0000-000000000000' AND c.observatory_id = observatoryId WITH DISTINCT (c.observatory_id) as cobservatoryId, count(c) as comparisons, resources, observatoryId RETURN observatoryId, resources, comparisons""".trimIndent()
        return client
            .query(query)
            .fetchAs(ObservatoryResources::class.java)
            .mappedBy(obsResDataMapper)
            .all()
    }

    override fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<TopContributorIdentifiers> {
        val query =  """MATCH(sub: Resource) WHERE ('Paper' IN LABELS(sub) OR 'Comparison' IN LABELS(sub) OR 'Problem' IN LABELS(sub) OR 'Contribution' IN LABELS(sub) OR 'Visualization' IN LABELS(sub)) AND (sub.created_by <> '00000000-0000-0000-0000-000000000000' AND sub.created_at > '$date' )  """.trimIndent()
        val count = extractQueryCount(query, "RETURN DISTINCT COUNT(sub)", pageable)
        val result = queryContributors(query, pageable)

        return PageImpl(result, pageable, count)
    }

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ResourceId,
        date: String
    ): Iterable<Map<String, Iterable<ResultObject>>> {
        val query =  """
           MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField) WHERE research1.resource_id='$id' WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND c.created_by IS NOT NULL AND c.created_by <> '00000000-0000-0000-0000-000000000000' AND c.created_at > '$date'  WITH  c.created_by AS contribution_creators, COUNT(c.created_by) AS cnt RETURN COLLECT({id:contribution_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField) WHERE research1.resource_id='$id' WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND comparison1.created_by IS NOT NULL AND comparison1.created_by <> '00000000-0000-0000-0000-000000000000' AND comparison1.created_at > '$date' WITH  comparison1.created_by AS comparison_creators, COUNT(comparison1.created_by) AS cnt RETURN COLLECT({id:comparison_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField) WHERE research1.resource_id='$id' WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND p1.created_by IS NOT NULL AND p1.created_by <> '00000000-0000-0000-0000-000000000000' AND p1.created_at > '$date' WITH  p1.created_by AS paper_creators, COUNT(p1.created_by) AS cnt RETURN COLLECT({id:paper_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField) WHERE research1.resource_id='$id' WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH (v:Visualization)<-[:RELATED]-(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND v.created_by IS NOT NULL AND v.created_by <> '00000000-0000-0000-0000-000000000000' AND v.created_at > '$date' WITH  v.created_by AS visualization_creators, COUNT(v.created_by) AS cnt RETURN COLLECT({id:visualization_creators, cnt:cnt}) AS total UNION MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField) WHERE research1.resource_id='$id' WITH (COLLECT(research) + COLLECT(research1)) AS r OPTIONAL MATCH (problem:Problem)<-[:RELATED]-(c:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(inner_r) WHERE inner_r in r AND problem.created_by IS NOT NULL AND problem.created_by <> '00000000-0000-0000-0000-000000000000' AND problem.created_at > '$date' WITH  problem.created_by AS problem_creators, COUNT(problem.created_by) AS cnt RETURN COLLECT({id:problem_creators, cnt:cnt}) AS total
       """.trimIndent()

        val result = client
            .query(query)
            .fetch()
            .all()

        return result as Iterable<Map<String, Iterable<ResultObject>>>
    }

    override fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse> {
        val query =  """MATCH (sub: Thing) WHERE ('Paper' IN labels(sub) OR 'Comparison' IN labels(sub) OR 'Problem' IN labels(sub) OR 'Visualization' IN labels(sub) OR 'Contribution' IN labels(sub)) AND (NOT 'PaperDeleted' IN  labels(sub)) AND (NOT 'ContributionDeleted' IN labels(sub)) """.trimIndent()
        val count = extractQueryCount(query, "WITH sub.resource_id AS id RETURN COUNT(id)", pageable)
        val result = queryCL(query, pageable)

        return PageImpl(result, pageable, count)
    }

    override fun getChangeLogByResearchField(
        id: ResourceId,
        pageable: Pageable
    ): Page<ChangeLogResponse> {
        val query =  """MATCH (comp:Comparison)-[:RELATED* 0..4]->(contribution:Contribution)<-[:RELATED* 0..4]-(p:Paper)-[:RELATED* 0..4]->(r:ResearchField) OPTIONAL MATCH(v:Visualization)<-[:RELATED* 0..4]-(comp) WHERE r.resource_id='$id' WITH COLLECT([v.resource_id,v.label, v.created_at, v.created_by,labels(v)])  +  COLLECT([comp.resource_id,comp.label, comp.created_at, comp.created_by, labels(comp)]) + COLLECT([contribution.resource_id,contribution.label, contribution.created_at, contribution.created_by, labels(contribution)]) + COLLECT([p.resource_id,p.label, p.created_at, p.created_by, labels(p)]) + COLLECT([r.resource_id,r.label, r.created_at, r.created_by, labels(r)]) AS items UNWIND items AS changelogs  """.trimIndent()
        val count = extractQueryCount(query, "WITH DISTINCT changelogs WHERE changelogs[0] IS NOT NULL RETURN COUNT(changelogs)", pageable)
        val result = queryChangeLogByResearchField(query, pageable)

        return PageImpl(result, pageable, count)
    }

    override fun getTrendingResearchProblems(pageable: Pageable): Page<eu.tib.orkg.prototype.statements.ports.TrendingResearchProblems> {
        val query =  """MATCH (paper: Paper)-[:RELATED {predicate_id: 'P31'}]->(c1: Contribution)-[:RELATED{predicate_id: 'P32'}]-> (r:Problem) WHERE paper.created_by <> '00000000-0000-0000-0000-000000000000' WITH r.resource_id AS id, r.label AS researchProblem, COUNT(paper) AS papersCount, COLLECT(DISTINCT paper.created_by) AS contributor""".trimIndent()
        val count = extractQueryCount(query, "RETURN COUNT(id)", pageable)
        val result = queryTRP(query, pageable)

        return PageImpl(result, pageable, count)

    }

    private fun extractQueryCount(mainQuery: String, returnQuery: String, pageable: Pageable): Long =
        client.query("$mainQuery $returnQuery ${pageable.toCypher()}")
            .fetchAs<Long>().one() ?: 0

    private fun queryTRP(
        query: String,
        pageable: Pageable
    ): List<TrendingResearchProblems> {
        val result = client.query("$query RETURN id, researchProblem, papersCount ${pageable.toCypher()}")
            .fetchAs(TrendingResearchProblems::class.java)
            .mappedBy(trpDataMapper)
            .all()

        return result.map { toTrendingResearchProblems(it) }
    }

    private fun queryCL(
        query: String,
        pageable: Pageable
    ): List<ChangeLogResponse> {
        val result = client.query("$query RETURN sub.resource_id AS id, sub.label AS label, sub.created_at AS createdAt, COALESCE(sub.created_by, '00000000-0000-0000-0000-000000000000') as createdBy, labels(sub) AS classes ORDER BY createdAt DESC ${pageable.toCypher()}")
            .fetchAs(ChangeLogResponse::class.java)
            .mappedBy(clpDataMapper)
            .all()

        return result.map { toChangeLogResponse(it) }
    }

    private fun queryContributors(
        query: String,
        pageable: Pageable
    ): List<TopContributorIdentifiers> {
        val result = client.query("$query RETURN DISTINCT sub.created_by AS id, count(sub) AS contributions ${pageable.toCypher()}")
            .fetchAs(TopContributorIdentifiers::class.java)
            .mappedBy(contributorsDataMapper)
            .all()

        return result.map { toContributors(it) }
    }

    private fun queryChangeLogByResearchField(
        query: String,
        pageable: Pageable
    ): List<ChangeLogResponse> {
        val result = client.query("$query WITH DISTINCT changelogs[0] AS id, changelogs[1] AS label, changelogs[2] AS createdAt, changelogs[3] AS createdBy, changelogs[4] AS classes WHERE id IS NOT NULL RETURN id, label, createdAt, createdBy, classes ${pageable.toCypher()}")
            .fetchAs(ChangeLogResponse::class.java)
            .mappedBy(clpDataMapper)
            .all()

        return result.map { toChangeLogResponse(it) }
    }

    private val trpDataMapper = { _: TypeSystem, record: Record ->
        TrendingResearchProblems(
            id = record["id"].asString(),
            researchProblem = record["researchProblem"].asString(),
            papersCount = record["papersCount"].asLong()
        )
    }
}


private fun toTrendingResearchProblems(trp: TrendingResearchProblems) =
    TrendingResearchProblems(
        id = trp.id,
        researchProblem = trp.researchProblem,
        papersCount = trp.papersCount
    )


private fun toChangeLogResponse(clp: ChangeLogResponse) =
    ChangeLogResponse(
        id = clp.id,
        label = clp.label,
        createdAt = clp.createdAt,
        createdBy = clp.createdBy,
        classes = clp.classes
    )

private fun toContributors(contributors: TopContributorIdentifiers) =
    TopContributorIdentifiers(
        id = contributors.id,
        contributions = contributors.contributions
    )

private fun toFieldStats(fs: FieldsStats) =
    FieldsStats(
        fieldId = fs.fieldId,
        field = fs.field,
        papers = fs.papers
    )

private val clpDataMapper = { _: TypeSystem, record: Record ->
    ChangeLogResponse(
        id = record["id"].asString(),
        label = record["label"].asString(),
        createdAt = record["createdAt"].asString(),
        createdBy = record["createdBy"].asString(),
        classes = record["classes"].asList() as List<String>
    )
}

private val fsDataMapper = { _: TypeSystem, record: Record ->
    FieldsStats(
        fieldId = record["fieldId"].asString(),
        field = record["field"].asString(),
        papers = record["papers"].asLong()
    )
}

private val obsResDataMapper = { _: TypeSystem, record: Record ->
    ObservatoryResources(
        observatoryId = record["observatoryId"].asString(),
        resources = record["resources"].asLong(),
        comparisons = record["comparisons"].asLong()
    )
}

private val contributorsDataMapper = { _: TypeSystem, record: Record ->
    TopContributorIdentifiers(
        id = record["id"].asString(),
        contributions = record["contributions"].asLong()
    )
}

private val statisticsDataMapper = { _: TypeSystem, record: Record ->  record.asMap() }
