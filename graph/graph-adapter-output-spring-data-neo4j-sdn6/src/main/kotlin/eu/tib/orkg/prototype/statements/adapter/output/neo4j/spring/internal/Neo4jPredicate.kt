package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.Predicate
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
