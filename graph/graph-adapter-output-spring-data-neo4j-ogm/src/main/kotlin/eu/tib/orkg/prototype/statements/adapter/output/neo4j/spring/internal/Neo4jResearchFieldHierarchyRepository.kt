package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jResearchFieldHierarchyRepository : Neo4jRepository<Neo4jResource, Long> {
    @Query("""
MATCH (p:ResearchField {id: $id})-[:RELATED {predicate_id: "P36"}]->(c:ResearchField)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: "P36"}]->(g:ResearchField)
WITH c AS resource, COUNT(g) AS childCount
ORDER BY resource.id ASC
RETURN resource, childCount""",
        countQuery = """
MATCH (p:ResearchField {id: $id})-[:RELATED {predicate_id: "P36"}]->(c:ResearchField)
RETURN COUNT(c) as cnt""")
    fun findChildren(id: ThingId, pageable: Pageable): Page<Neo4jResearchFieldWithChildCount>

    @Query("""
MATCH (:ResearchField {id: $id})<-[:RELATED {predicate_id: "P36"}]-(p:ResearchField)
WITH p
ORDER BY p.id ASC
RETURN p""",
        countQuery = """
MATCH (:ResearchField {id: $id})<-[:RELATED {predicate_id: "P36"}]-(p:ResearchField)
RETURN COUNT(p)""")
    fun findParents(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""
MATCH (:ResearchField {id: $id})<-[:RELATED* {predicate_id: "P36"}]-(r:ResearchField)
WHERE NOT (r)<-[:RELATED {predicate_id: "P36"}]-(:ResearchField)
RETURN DISTINCT r""",
        countQuery = """
MATCH (:ResearchField {id: $id})<-[:RELATED* {predicate_id: "P36"}]-(r:ResearchField)
WHERE NOT (r)<-[:RELATED {predicate_id: "P36"}]-(:ResearchField)
RETURN COUNT(DISTINCT r)""")
    fun findRoots(id: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query("""
MATCH (r:ResearchField)
WHERE NOT (:ResearchField)-[:RELATED {predicate_id: "P36"}]->(r)
WITH r
ORDER BY r.id ASC
RETURN r""",
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
RETURN researchField AS resource, parentIds""",
        countQuery = """
MATCH (c:ResearchField {id: $id})<-[:RELATED*0.. {predicate_id: "P36"}]-(p:ResearchField)
WITH COLLECT(p) + COLLECT(c) AS researchFields
UNWIND researchFields AS researchField
RETURN COUNT(DISTINCT researchField)""")
    fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<Neo4jResearchFieldHierarchyEntry>
}

@QueryResult
data class Neo4jResearchFieldWithChildCount(
    val resource: Neo4jResource,
    val childCount: Long
)

@QueryResult
data class Neo4jResearchFieldHierarchyEntry(
    val resource: Neo4jResource,
    val parentIds: Set<String>
)
