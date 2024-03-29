package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.TemplatedResource
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

interface Neo4jFormattedLabelRepository : Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""
MATCH (res:Resource {id: $id})
WITH [x IN LABELS(res) WHERE NOT x IN ['Thing', 'Resource']][0] AS classId, res
MATCH (format:Literal)<-[:RELATED {predicate_id:'TemplateLabelFormat'}]-(template:NodeShape)-[:RELATED {predicate_id:'sh:targetClass'}]->(cls:Class {id: classId}) 
MATCH (res)-[p:RELATED]->(o:Thing)
WITH COLLECT(p.predicate_id) AS predicates, COLLECT(o.label) AS values, classId, format.label AS format, template.id AS id, template.label AS label
RETURN id, label, classId, format, predicates, values
    """)
    fun findTemplateSpecs(id: ThingId): Optional<TemplatedResource>
}
