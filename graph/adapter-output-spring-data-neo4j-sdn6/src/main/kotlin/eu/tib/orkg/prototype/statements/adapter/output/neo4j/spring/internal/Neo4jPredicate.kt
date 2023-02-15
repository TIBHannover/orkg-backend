package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship.*

@Node("Predicate")
class Neo4jPredicate : Neo4jThing {

    @Id
    @Property("predicate_id")
    var predicateId: PredicateId? = null

    @Property("label")
    override var label: String? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

//    @Relationship(type = "RELATED", direction = Direction.OUTGOING)
//    var subjectOf: MutableList<Neo4jStatement> = mutableListOf()

    fun toPredicate() = Predicate(
        id = predicateId,
        label = label!!,
        createdAt = createdAt!!,
        createdBy = createdBy
    )

    override val thingId: String?
        get() = predicateId?.value

    override fun toThing() = toPredicate()
}
