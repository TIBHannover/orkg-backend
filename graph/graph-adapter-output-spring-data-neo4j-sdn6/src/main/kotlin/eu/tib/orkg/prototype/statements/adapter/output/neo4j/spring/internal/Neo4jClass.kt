package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.Class
import java.net.URI
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

@Node("Class")
class Neo4jClass : Neo4jThing() {
    @Property("uri")
    var uri: String? = null

    @Relationship(type = "RELATED", direction = Relationship.Direction.OUTGOING)
    var statements: MutableList<Neo4jStatement> = mutableListOf()

    fun toClass() = Class(
        id = id!!,
        label = label!!,
        uri = if (uri != null) URI.create(uri!!) else null,
        createdAt = created_at!!,
        createdBy = created_by,
        description = statements.singleOrNull { it.predicateId == Predicates.description }?.targetNode?.label
    )

    override fun toThing() = toClass()
}
