package eu.tib.orkg.prototype.statements.domain.model.neo4j

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
}

@QueryResult
data class ProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long
)
