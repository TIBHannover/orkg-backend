package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jResearchFieldRepository :
    Neo4jRepository<Neo4jStatement, Long> {

    @Query("""MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                    WITH COUNT(paper) AS papers, problem
                    RETURN DISTINCT problem, papers
                    ORDER BY papers DESC""",
        countQuery = """MATCH (field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                        WITH COUNT(paper) AS papers, problem
                        RETURN COUNT(papers) AS cnt"""
    )
    fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<ProblemsPerField>

    @Query("""MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p2:Paper)-[:RELATED{predicate_id: "P31"}]->(contribution2:Contribution)<-[:RELATED]-(comparison2:Comparison) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) + COLLECT(comparison2.created_by) + COLLECT(contribution2.created_by) + COLLECT(p2.created_by) AS items 
                    UNWIND items AS orkgusers 
                    RETURN DISTINCT orkgusers
                    ORDER BY orkgusers DESC""",
        countQuery = """MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p2:Paper)-[:RELATED{predicate_id: "P31"}]->(contribution2:Contribution)<-[:RELATED]-(comparison2:Comparison) 
                        WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) + COLLECT(comparison2.created_by) + COLLECT(contribution2.created_by) + COLLECT(p2.created_by) AS items 
                        UNWIND items AS orkgusers
                        RETURN COUNT(DISTINCT orkgusers) AS cnt"""
    )
    fun getContributorsFromResearchFieldAndIncludeSubfields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH(p:Paper)-[:RELATED {predicate_id: "P30"}]->(r: ResearchField{resource_id:{0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: "P30"}]-(p1:Paper) 
                    WITH COLLECT(p) + COLLECT(p1) AS items 
                    UNWIND items AS papers 
                    RETURN DISTINCT papers
                    ORDER BY papers DESC""",
        countQuery = """MATCH(p:Paper)-[:RELATED {predicate_id: "P30"}]->(r: ResearchField{resource_id:{0}})<-[:RELATED* {predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: "P30"}]-(p1:Paper) 
                    WITH COLLECT(p) + COLLECT(p1) AS items 
                    UNWIND items AS papers 
                    RETURN count(DISTINCT papers) AS cnt""")
    fun getListOfPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (problem1:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(r1:ResearchField)-[:RELATED* {predicate_id: "P36"}]->(field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) 
                    WITH COLLECT(problem) + COLLECT(problem1) AS items 
                    UNWIND items AS orkgproblems 
                    RETURN DISTINCT orkgproblems 
                    ORDER BY orkgproblems DESC""",
        countQuery = """MATCH (problem1:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(r1:ResearchField)-[:RELATED* {predicate_id: "P36"}]->(field:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) 
                    WITH COLLECT(problem) + COLLECT(problem1) AS items 
                    UNWIND items AS orkgproblems 
                    RETURN COUNT(DISTINCT orkgproblems) AS cnt""")
    fun getProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(c: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})<-[:RELATED*{predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(:Paper)-[:RELATED{predicate_id: "P31"}]->(:Contribution)<-[:RELATED]-(c1:Comparison) 
                    WITH COLLECT(c) + collect(c1) AS items 
                    UNWIND items AS orkgcomparisons 
                    RETURN DISTINCT orkgcomparisons
                    ORDER BY orkgcomparisons DESC""",
        countQuery = """MATCH(c: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})<-[:RELATED*{predicate_id: "P36"}]-(:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(:Paper)-[:RELATED{predicate_id: "P31"}]->(:Contribution)<-[:RELATED]-(c1:Comparison) 
                    WITH COLLECT(c) + collect(c1) AS items 
                    UNWIND items AS orkgcomparisons 
                    RETURN COUNT(DISTINCT orkgcomparisons)""")
    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers 
                    RETURN DISTINCT orkgusers
                    ORDER BY orkgusers DESC""",
    countQuery = """MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: {0}}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN COUNT(DISTINCT orkgusers) as cnt""")
    fun getListOfContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH(paper:Paper)-[:RELATED {predicate_id: "P30"}]->(r: ResearchField{resource_id:{0}})	
	            RETURN DISTINCT paper
                ORDER BY paper DESC""",
    countQuery = """MATCH(paper:Paper)-[:RELATED {predicate_id: "P30"}]->(r: ResearchField{resource_id:{0}})	
                RETURN COUNT(DISTINCT paper) AS cnt""")
    fun getListOfPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})
	            RETURN DISTINCT comparison
                ORDER BY comparison DESC""",
    countQuery = """MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: {0}})
	            RETURN COUNT(DISTINCT comparison)""")
    fun getListOfComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>
}

@QueryResult
data class ProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long
)
