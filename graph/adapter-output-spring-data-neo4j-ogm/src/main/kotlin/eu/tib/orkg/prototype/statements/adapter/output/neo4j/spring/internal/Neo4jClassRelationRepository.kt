package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.*

private const val childId = "${'$'}childId"
private const val relation = "SUBCLASS_OF"

interface Neo4jClassRelationRepository : Neo4jRepository<Neo4jClassRelation, Long> {
    @Query("""MATCH (:Class {class_id: $childId})-[r:$relation]->(:Class) RETURN r""")
    fun findByChildClassId(childId: ClassId?): Optional<Neo4jClassRelation>

    @Query("""MATCH (:Class {class_id: $childId})-[r:$relation]->(:Class) DELETE r""")
    fun removeByChildClassId(childId: ClassId)
}
