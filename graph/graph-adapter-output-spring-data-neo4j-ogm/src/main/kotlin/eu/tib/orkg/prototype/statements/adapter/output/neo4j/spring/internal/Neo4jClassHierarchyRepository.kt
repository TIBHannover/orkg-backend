package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"
private const val childId = "${'$'}childId"
private const val relation = "SUBCLASS_OF"

interface Neo4jClassHierarchyRepository : Neo4jRepository<Neo4jClass, Long> {
    @Query("""MATCH (p:Class {id: $id})<-[:$relation]-(c:Class) OPTIONAL MATCH (c)<-[:$relation]-(g:Class) RETURN c AS class, count(g) AS childCount ORDER BY class.id ASC""",
        countQuery = """MATCH (:Class {id: $id})<-[:$relation]-(c:Class) RETURN COUNT(c) as cnt""")
    fun findChildren(id: ThingId, pageable: Pageable): Page<Neo4jChildClass>

    @Query("""MATCH (:Class {id: $id})-[:$relation]->(p:Class) return p""")
    fun findParent(id: ThingId): Optional<Neo4jClass>

    @Query("""MATCH (:Class {id: $id})-[:$relation*]->(r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN r""")
    fun findRootClass(id: ThingId): Optional<Neo4jClass>

    @Query("""MATCH (r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN r ORDER BY r.id ASC""",
        countQuery = """MATCH (r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN COUNT(r) as cnt""")
    fun findAllRoots(pageable: Pageable): Page<Neo4jClass>

    @Query("""MATCH (c:Class {id: $childId}) RETURN EXISTS((c)-[:$relation*]->(:Class {id: $id})) as exists""")
    fun existsChild(id: ThingId, childId: ThingId): Boolean

    @Query("""MATCH (c:Class {id: $id}) RETURN EXISTS((c)<-[:$relation]-(:Class)) as exists""")
    fun existsChildren(id: ThingId): Boolean

    @Query("""
MATCH (:Class {id: $id})-[:$relation*0..]->(c:Class)
WITH COLLECT(c) AS classes
UNWIND classes AS class
WITH DISTINCT class
OPTIONAL MATCH (class)-[:$relation]->(p:Class)
WITH class, p.id AS parentId
ORDER BY class.id ASC
RETURN class, parentId""",
        countQuery = """
MATCH (:Class {id: $id})-[:$relation*0..]->(c:Class)
WITH COLLECT(c) AS classes
UNWIND classes AS class
RETURN COUNT(DISTINCT class)""")
    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<Neo4jClassHierarchyEntry>

    @Query("""MATCH (:Class {id: $id})<-[:$relation*0..]-(c:Class) WITH COLLECT(c.id) AS ids MATCH (i:Thing) WHERE ANY(label IN LABELS(i) WHERE label IN ids) RETURN COUNT(i)""")
    fun countClassInstances(id: ThingId): Long
}

@QueryResult
data class Neo4jChildClass(
    val `class`: Neo4jClass,
    val childCount: Long
)

@QueryResult
data class Neo4jClassHierarchyEntry(
    val `class`: Neo4jClass,
    val parentId: String?
)
