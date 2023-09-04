package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction

@Node("Predicate")
class Neo4jPredicate : Neo4jThing() {
    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Relationship(type = "RELATED", direction = Direction.OUTGOING)
    var statements: MutableList<Neo4jStatement> = mutableListOf()

    fun toPredicate() = Predicate(
        id = id!!,
        label = label!!,
        createdAt = createdAt!!,
        createdBy = createdBy,
        description = statements.singleOrNull { it.predicateId == Predicates.description }?.targetNode?.label
    )

    override fun toThing() = toPredicate()
}
