package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jResearchFieldHierarchyRepository : Neo4jRepository<Neo4jResource, ThingId> {
    @Query("""
MATCH (p:ResearchField {id: $id})-[:RELATED {predicate_id: "P36"}]->(c:ResearchField)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "P36"}]->(g:ResearchField)
WITH c AS resource, COUNT(g) AS childCount
ORDER BY resource.id ASC
RETURN resource, childCount $PAGE_PARAMS""",
        countQuery = """
MATCH (p:ResearchField {id: $id})-[:RELATED {predicate_id: "P36"}]->(c:ResearchField)
RETURN COUNT(c) as cnt""")
    fun findAllChildrenByAncestorId(id: ThingId, pageable: Pageable): Page<Neo4jResearchFieldWithChildCount>

    @Query("""
MATCH (:ResearchField {id: $id})<-[:RELATED {predicate_id: "P36"}]-(p:ResearchField)
WITH p
ORDER BY p.id ASC
RETURN p $PAGE_PARAMS""",
        countQuery = """
MATCH (:ResearchField {id: $id})<-[:RELATED {predicate_id: "P36"}]-(p:ResearchField)
RETURN COUNT(p)""")
    fun findAllParentsByChildId(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""
MATCH (:ResearchField {id: $id})<-[:RELATED* {predicate_id: "P36"}]-(r:ResearchField)
WHERE NOT (r)<-[:RELATED {predicate_id: "P36"}]-(:ResearchField)
RETURN DISTINCT r $PAGE_PARAMS""",
        countQuery = """
MATCH (:ResearchField {id: $id})<-[:RELATED* {predicate_id: "P36"}]-(r:ResearchField)
WHERE NOT (r)<-[:RELATED {predicate_id: "P36"}]-(:ResearchField)
RETURN COUNT(DISTINCT r)""")
    fun findAllRootsByDescendantId(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""
MATCH (r:ResearchField)
WHERE NOT (:ResearchField)-[:RELATED {predicate_id: "P36"}]->(r)
WITH r
ORDER BY r.id ASC
RETURN r $PAGE_PARAMS""",
        countQuery = """
MATCH (r:ResearchField)
WHERE NOT (:ResearchField)-[:RELATED {predicate_id: "P36"}]->(r)
RETURN COUNT(r) AS cnt""")
    fun findAllRoots(pageable: Pageable): Page<Neo4jResource>

    @Query("""
MATCH (c:ResearchField {id: $id})<-[:RELATED*0.. {predicate_id: "P36"}]-(p:ResearchField)
WITH COLLECT(p) + COLLECT(c) AS researchFields
UNWIND researchFields AS researchField
WITH DISTINCT researchField
OPTIONAL MATCH (researchField)<-[:RELATED {predicate_id: "P36"}]-(p:ResearchField)
WITH researchField, COLLECT(p.id) AS parentIds
ORDER BY researchField.id ASC
RETURN researchField AS resource, parentIds $PAGE_PARAMS""",
        countQuery = """
MATCH (c:ResearchField {id: $id})<-[:RELATED*0.. {predicate_id: "P36"}]-(p:ResearchField)
WITH COLLECT(p) + COLLECT(c) AS researchFields
UNWIND researchFields AS researchField
RETURN COUNT(DISTINCT researchField)""")
    fun findResearchFieldHierarchyByResearchFieldId(id: ThingId, pageable: Pageable): Page<Neo4jResearchFieldHierarchyEntry>
}

data class Neo4jResearchFieldWithChildCount(
    val resource: Neo4jResource,
    val childCount: Long
)

data class Neo4jResearchFieldHierarchyEntry(
    val resource: Neo4jResource,
    val parentIds: Set<String>
)
