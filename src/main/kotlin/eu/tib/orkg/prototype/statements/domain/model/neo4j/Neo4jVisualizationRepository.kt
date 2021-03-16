package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.neo4j.ogm.annotation.Property
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jVisualizationRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (research:ResearchField)<-[:RELATED*0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: {0}}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields  MATCH(v:Visualization)<-[:RELATED]-(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH v.resource_id AS id, v.label AS label, v.created_at AS created_at, comp.resource_id AS comparison_id, v.description AS description, comp.label AS comparison_label, v.created_by AS created_by RETURN DISTINCT id, label, created_at, comparison_id, description, comparison_label, created_by""",
        countQuery = "MATCH (research:ResearchField)<-[:RELATED*0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: {0}}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields  MATCH(v:Visualization)<-[:RELATED]-(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT v.resource_id) AS cnt")
    fun findVisResourceIdsIncludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationResAndMetaInfo>

    @Query("MATCH(v:Visualization)<-[:RELATED]-(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(r: ResearchField{resource_id: {0}}) WITH v.resource_id AS id, v.label AS label, v.created_at AS created_at, comp.resource_id AS comparison_id, v.description AS description, comp.label AS comparison_label, v.created_by AS created_by RETURN DISTINCT id, label, created_at, comparison_id, description, comparison_label, created_by",
        countQuery = "MATCH(v:Visualization)<-[:RELATED]-(comp:Comparison)-[:RELATED]->(contribution:Contribution)<-[:RELATED]-(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(r: ResearchField{resource_id: {0}}) RETURN COUNT(DISTINCT v.resource_id) AS cnt")
    fun findVisResourceIdsExcludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationResAndMetaInfo>
}

@QueryResult
data class VisualizationResAndMetaInfo(
    val id: String? = null,
    val label: String? = null,
    @Property("created_at")
    val createdAt: String? = null,
    @Property("comparison_id")
    val comparisonId: String? = null,
    val description: String? = null,
    @Property("comparison_label")
    val comparisonLabel: String? = null,
    @Property("created_by")
    val createdBy: String = "00000000-0000-0000-0000-000000000000"
)
