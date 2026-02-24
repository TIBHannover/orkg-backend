package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ID = "${'$'}id"
private const val PREDICATE_IDS = "${'$'}predicateIds"
private const val ROSETTA_STONE_TEMPLATE_IDS = "${'$'}rosettaStoneStatementIds"
private const val MAX_DEPTH = "${'$'}maxDepth"

interface Neo4jComparisonAuxiliaryRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query(
        """
MATCH (:Comparison {id: $ID})-[:RELATED {predicate_id: "compareContribution"}]->(n:Resource)
WITH COLLECT(n) AS roots
CALL custom.subgraph(roots, {maxLevel: $MAX_DEPTH, relationshipFilter: "RELATED>", labelFilter: "+Thing", bfs: false})
YIELD relationships AS rels
UNWIND rels AS rel
MATCH (p:Predicate {id: rel.predicate_id})
OPTIONAL MATCH (p)-[:RELATED {predicate_id: "description"}]->(desc_literal:Literal)
RETURN startNode(rel).id AS subjectId, rel.predicate_id AS predicateId, desc_literal.label AS description, COLLECT(endNode(rel).id) AS objectIds, p.label AS predicateLabel, "PREDICATE" AS type
UNION ALL
MATCH (:Comparison {id: $ID})-[:RELATED {predicate_id: "compareRosettaStoneContribution"}]->(n:Resource)<-[:CONTEXT]-(:RosettaStoneStatement)-[:TEMPLATE]->(template:RosettaNodeShape)-[:RELATED {predicate_id: "description"}]->(template_desc_literal:Literal)
WITH DISTINCT template, n, template_desc_literal.label AS template_desc
CALL (template, n, template_desc) {
    MATCH (template)-[:RELATED {predicate_id: "sh:property"}]->(s:PropertyShape)
    MATCH (s)-[:RELATED {predicate_id: "placeholder"}]->(placeholder:Literal), (s)-[:RELATED {predicate_id: "sh:order"}]->(order:Literal)
    OPTIONAL MATCH (s)-[:RELATED {predicate_id: "description"}]->(property_desc_literal:Literal)
    RETURN template.id AS subjectId, CASE WHEN order.label = "0" THEN "hasSubjectPosition" ELSE "hasObjectPosition" + order.label END AS predicateId, property_desc_literal.label AS description, [] AS objectIds, placeholder.label AS predicateLabel, "ROSETTA_STONE_STATEMENT_VALUE" AS type
    UNION
    RETURN n.id AS subjectId, template.id AS predicateId, template_desc AS description, [template.id] AS objectIds, template.label AS predicateLabel, "ROSETTA_STONE_STATEMENT" AS type
}
RETURN subjectId, predicateId, description, objectIds, predicateLabel, type
"""
    )
    fun findAllComparisonTablePredicatePathsByComparisonId(id: ThingId, maxDepth: Int): Set<Neo4jComparisonPathEntry>

    @Query(
        """
UNWIND $PREDICATE_IDS AS id
MATCH (n:Thing {id: id})
OPTIONAL MATCH (n)-[:RELATED {predicate_id: "description"}]->(desc_literal:Literal)
RETURN n.id AS predicateId, NULL AS templateId, n.label AS predicateLabel, desc_literal.label AS description, "PREDICATE" AS type
UNION ALL
UNWIND $ROSETTA_STONE_TEMPLATE_IDS AS id
MATCH (template:RosettaNodeShape {id: id})
CALL (template) {
    MATCH (template)-[:RELATED {predicate_id: "sh:property"}]->(s:PropertyShape)
    MATCH (s)-[:RELATED {predicate_id: "placeholder"}]->(placeholder:Literal)
    MATCH (s)-[:RELATED {predicate_id: "sh:order"}]->(order:Literal)
    OPTIONAL MATCH (s)-[:RELATED {predicate_id: "description"}]->(property_desc_literal:Literal)
    RETURN CASE WHEN order.label = "0" THEN "hasSubjectPosition" ELSE "hasObjectPosition" + order.label END AS predicateId, template.id AS templateId, placeholder.label AS predicateLabel, property_desc_literal.label AS description, "ROSETTA_STONE_STATEMENT_VALUE" AS type
    UNION ALL
    MATCH (template)-[:RELATED {predicate_id: "description"}]->(template_desc_literal:Literal)
    RETURN template.id AS predicateId, NULL AS templateId, template.label AS predicateLabel, template_desc_literal.label AS description, "ROSETTA_STONE_STATEMENT" AS type
}
RETURN predicateId, templateId, predicateLabel, description, type"""
    )
    fun findComparisonPathLabelsByThingIdsAndRosettaStoneTemplateIds(
        predicateIds: Set<ThingId>,
        rosettaStoneStatementIds: Set<ThingId>,
    ): Set<Neo4jComparisonPathLabelEntry>
}

data class Neo4jComparisonPathEntry(
    val subjectId: ThingId,
    val predicateId: ThingId,
    val description: String?,
    val objectIds: List<ThingId>,
    val predicateLabel: String,
    val type: ComparisonPath.Type,
)

data class Neo4jComparisonPathLabelEntry(
    val predicateId: ThingId,
    val templateId: ThingId?,
    val predicateLabel: String,
    val description: String?,
    val type: ComparisonPath.Type,
)
