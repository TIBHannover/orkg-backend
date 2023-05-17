package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val childId = "${'$'}childId"
private const val relation = "SUBCLASS_OF"

interface Neo4jClassRelationRepository : Neo4jRepository<Neo4jClassRelation, Long> {
    @Query("""MATCH (:Class {id: $childId})-[r:$relation]->(:Class) RETURN r""")
    fun findByChildId(childId: ThingId?): Optional<Neo4jClassRelation>

    @Query("""MATCH (:Class {id: $childId})-[r:$relation]->(:Class) DELETE r""")
    fun removeByChildId(childId: ThingId)
}
