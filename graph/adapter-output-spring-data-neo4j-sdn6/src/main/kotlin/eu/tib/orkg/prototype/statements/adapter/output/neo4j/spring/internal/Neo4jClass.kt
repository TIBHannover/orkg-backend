package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction

@Node("Class")
class Neo4jClass : Neo4jThing {
    @Id
    @Property("class_id")
    var classId: ClassId? = null

    @Property("label")
    override var label: String? = null

    @Relationship(type = "RELATED", direction = Direction.OUTGOING)
    var subjectOf: MutableSet<Neo4jClass> = mutableSetOf()

    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        val clazz = Class(
            id = ThingId(classId!!.value),
            label = label!!,
            uri = aURI,
            createdAt = createdAt!!,
            createdBy = createdBy,
        )
        if (subjectOf.isNotEmpty())
            clazz.description = subjectOf.firstOrNull { it.classId?.value == "description" }?.label
        return clazz
    }

    override val thingId: String?
        get() = classId?.value

    override fun toThing() = toClass()
}
