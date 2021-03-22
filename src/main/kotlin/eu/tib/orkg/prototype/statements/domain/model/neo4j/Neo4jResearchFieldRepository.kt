package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jResearchFieldRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (field:ResearchField {resource_id: {0}}) RETURN field""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                    WITH COUNT(paper) AS papers, problem
                    RETURN DISTINCT problem, papers""",
        countQuery = """MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                        WITH COUNT(paper) AS papers, problem
                        RETURN COUNT(papers) AS cnt"""
    )
    fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<ProblemsPerField>

    @Query("""MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p2:Paper)-[:RELATED{predicate_id: "P31"}]->(contribution2:Contribution)<-[:RELATED]-(comparison2:Comparison) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) + COLLECT(comparison2.created_by) + COLLECT(contribution2.created_by) + COLLECT(p2.created_by) AS items 
                    UNWIND items AS orkgusers 
                    RETURN DISTINCT orkgusers""",
        countQuery = """MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p2:Paper)-[:RELATED{predicate_id: "P31"}]->(contribution2:Contribution)<-[:RELATED]-(comparison2:Comparison) 
                        WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) + COLLECT(comparison2.created_by) + COLLECT(contribution2.created_by) + COLLECT(p2.created_by) AS items 
                        UNWIND items AS orkgusers
                        RETURN COUNT(DISTINCT orkgusers) AS cnt"""
    )
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("MATCH(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:{0}})<-[:RELATED* {predicate_id: 'P36'}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p1:Paper) WITH COLLECT(p) + COLLECT(p1) AS items UNWIND items AS papers WITH DISTINCT papers, papers.created_at AS created_at, papers.resource_id AS resource_id, papers.label AS label, papers.created_by AS created_by RETURN papers",
        countQuery = "MATCH(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:{0}})<-[:RELATED* {predicate_id: 'P36'}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p1:Paper) WITH COLLECT(p) + COLLECT(p1) AS items UNWIND items AS papers RETURN count(DISTINCT papers) AS cnt")
    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(c: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})<-[:RELATED*{predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(:Paper)-[:RELATED{predicate_id: "P31"}]->(:Contribution)<-[:RELATED]-(c1:Comparison) 
                    WITH COLLECT(c) + collect(c1) AS items 
                    UNWIND items AS orkgcomparisons WITH DISTINCT orkgcomparisons, orkgcomparisons.resource_id AS resource_id, orkgcomparisons.label AS label, orkgcomparisons.created_by AS created_by, orkgcomparisons.created_at AS created_at RETURN orkgcomparisons""",
        countQuery = """MATCH(c: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})<-[:RELATED*{predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(:Paper)-[:RELATED{predicate_id: "P31"}]->(:Contribution)<-[:RELATED]-(c1:Comparison) 
                    WITH COLLECT(c) + collect(c1) AS items 
                    UNWIND items AS orkgcomparisons 
                    RETURN COUNT(DISTINCT orkgcomparisons)""")
    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (problem1:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(r1:ResearchField)-[:RELATED* {predicate_id: "P36"}]->(field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) 
                    WITH COLLECT(problem) + COLLECT(problem1) AS items UNWIND items AS orkgproblems WITH DISTINCT orkgproblems, orkgproblems.resource_id AS resource_id, orkgproblems.created_at AS created_at, orkgproblems.label AS label, orkgproblems.created_by AS created_by RETURN orkgproblems""",
        countQuery = """MATCH (problem1:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(r1:ResearchField)-[:RELATED* {predicate_id: 'P36'}]->(field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) 
                    WITH COLLECT(problem) + COLLECT(problem1) AS items 
                    UNWIND items AS orkgproblems WITH DISTINCT orkgproblems
                    RETURN COUNT(orkgproblems) AS cnt""")
    fun getProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN orkgusers""",
        countQuery = """MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN COUNT(orkgusers) as cnt""")
    fun getContributorIdsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("MATCH(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:{0}}) WITH DISTINCT paper, paper.created_at AS created_at, paper.resource_id AS resource_id, paper.label AS label, paper.created_by AS created_by RETURN paper",
        countQuery = "MATCH(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:{0}}) RETURN COUNT(paper) AS cnt")
    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = "MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})  WITH DISTINCT comparison, comparison.resource_id AS resource_id, comparison.label AS label, comparison.created_by AS created_by, comparison.created_at AS created_at RETURN comparison",
        countQuery = "MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}}) RETURN COUNT(comparison)")
    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) WITH DISTINCT problem, problem.created_at AS created_at, problem.resource_id AS resource_id, problem.created_by AS created_by RETURN problem """,
        countQuery = """MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) RETURN COUNT(DISTINCT problem) AS cnt""")
    fun getProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN DISTINCT r""",
        countQuery = """MATCH (:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN COUNT(DISTINCT r) AS cnt""")
    fun findResearchFieldsWithBenchmarks(): Iterable<Neo4jResource>
    }

@QueryResult
data class ProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long
)
