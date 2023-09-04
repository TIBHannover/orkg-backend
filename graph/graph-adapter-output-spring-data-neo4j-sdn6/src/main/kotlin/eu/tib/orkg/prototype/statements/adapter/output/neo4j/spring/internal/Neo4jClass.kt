package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.Class
import java.net.URI
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

@Node("Class")
class Neo4jClass : Neo4jThing() {
    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Relationship(type = "RELATED", direction = Relationship.Direction.OUTGOING)
    var statements: MutableList<Neo4jStatement> = mutableListOf()

    fun toClass() = Class(
        id = id!!,
        label = label!!,
        uri = if (uri != null) URI.create(uri!!) else null,
        createdAt = createdAt!!,
        createdBy = createdBy,
        description = statements.singleOrNull { it.predicateId == Predicates.description }?.targetNode?.label
    )

    override fun toThing() = toClass()
}
