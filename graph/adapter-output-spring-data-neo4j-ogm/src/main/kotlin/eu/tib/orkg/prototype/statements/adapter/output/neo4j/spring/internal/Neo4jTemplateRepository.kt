package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.Optional
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jTemplateRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (res:Resource {id: $id})
WITH [x IN LABELS(res) WHERE NOT x IN ['Thing', 'Resource']][0] AS class, res
MATCH (format:Literal)<-[:RELATED {predicate_id:'TemplateLabelFormat'}]-(template:ContributionTemplate)-[:RELATED {predicate_id:'TemplateOfClass'}]->(cls:Class {class_id: class}) 
MATCH (res)-[p:RELATED]->(o:Thing)
WITH COLLECT(p.predicate_id) AS predicates, COLLECT(o.label) AS values, class, format.label AS format, template.id AS id, template.label AS label
RETURN id, label, class, format, predicates, values
    """)
    fun findTemplateSpecs(id: ThingId): Optional<TemplatedResource>
}
