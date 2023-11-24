package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction

@Node("Predicate")
class Neo4jPredicate : Neo4jThing() {
    @Relationship(type = "RELATED", direction = Direction.OUTGOING)
    var statements: MutableList<Neo4jStatement> = mutableListOf()

    fun toPredicate() = Predicate(
        id = id!!,
        label = label!!,
        createdAt = created_at!!,
        createdBy = created_by,
        description = statements.singleOrNull { it.predicateId == Predicates.description }?.targetNode?.label
    )

    override fun toThing() = toPredicate()
}
