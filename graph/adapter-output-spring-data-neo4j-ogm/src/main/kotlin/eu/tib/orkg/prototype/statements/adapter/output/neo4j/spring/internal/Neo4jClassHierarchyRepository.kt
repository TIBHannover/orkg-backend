package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
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
    @Query("""MATCH (p:Class {class_id: $id})<-[:$relation]-(c:Class) OPTIONAL MATCH (c)<-[:$relation]-(g:Class) RETURN c AS class, count(g) AS childCount ORDER BY class.class_id ASC""",
        countQuery = """MATCH (:Class {class_id: $id})<-[:$relation]-(c:Class) RETURN COUNT(c) as cnt""")
    fun findChildren(id: ClassId, pageable: Pageable): Page<Neo4jChildClass>

    @Query("""MATCH (:Class {class_id: $id})-[:$relation]->(p:Class) return p""")
    fun findParent(id: ClassId): Optional<Neo4jClass>

    @Query("""MATCH (:Class {class_id: $id})-[:$relation*]->(r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN r""")
    fun findRootClass(id: ClassId): Optional<Neo4jClass>

    @Query("""MATCH (r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN r ORDER BY r.class_id ASC""",
        countQuery = """MATCH (r:Class) WHERE NOT (r)-[:$relation]->(:Class) RETURN COUNT(r) as cnt""")
    fun findAllRoots(pageable: Pageable): Page<Neo4jClass>

    @Query("""MATCH (c:Class {class_id: $childId}) RETURN EXISTS((c)-[:$relation*]->(:Class {class_id: $id})) as exists""")
    fun existsChild(id: ClassId, childId: ClassId): Boolean

    @Query("""MATCH (c:Class {class_id: $id}) RETURN EXISTS((c)<-[:$relation]-(:Class)) as exists""")
    fun existsChildren(id: ClassId): Boolean

    @Query("""MATCH (c:Class {class_id: $id})-[:$relation*0..]->(p:Class) WITH collect(p) + c AS classes UNWIND classes AS class WITH DISTINCT class OPTIONAL MATCH (class)-[:$relation]->(p:Class) RETURN class, p.class_id AS parentId ORDER BY class.class_id ASC""",
        countQuery = """MATCH (c:Class {class_id: $id})-[:$relation*0..]->(p:Class) WITH collect(p) + c AS classes RETURN COUNT(DISTINCT classes)""")
    fun findClassHierarchy(id: ClassId, pageable: Pageable): Page<Neo4jClassHierarchyEntry>

    @Query("""MATCH (r:Class {class_id: $id})<-[:$relation*]-(c:Class) WITH collect(c.class_id) + $id AS class_ids MATCH (i:Thing) WHERE ANY(label IN labels(i) WHERE label IN class_ids) RETURN count(i)""")
    fun countClassInstances(id: ClassId): Long
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
