package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.domain.TemplatedResource
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val RESOURCE_ID_TO_TEMPLATE_TARGET_CLASS = "${'$'}resourceIdToTemplateTargetClass"

interface Neo4jFormattedLabelRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query(
        """
UNWIND keys($RESOURCE_ID_TO_TEMPLATE_TARGET_CLASS) AS key
CALL (key) {
    MATCH (format:Literal)<-[:RELATED {predicate_id:'TemplateLabelFormat'}]-(template:NodeShape)-[:RELATED {predicate_id:'sh:targetClass'}]->(cls:Class {id: $RESOURCE_ID_TO_TEMPLATE_TARGET_CLASS[key]})
    MATCH (res:Resource {id: key})-[p:RELATED]->(o:Thing)
    RETURN key AS id, COLLECT(p.predicate_id) AS predicates, COLLECT(o.label) AS values, $RESOURCE_ID_TO_TEMPLATE_TARGET_CLASS[key] AS classId, format.label AS format, template.id AS templateId, template.label AS label
}
WITH id, templateId, label, classId, format, predicates, values
RETURN id, templateId, label, classId, format, predicates, values"""
    )
    fun findTemplateSpecs(resourceIdToTemplateTargetClass: Map<String, String>): List<TemplatedResource>
}
