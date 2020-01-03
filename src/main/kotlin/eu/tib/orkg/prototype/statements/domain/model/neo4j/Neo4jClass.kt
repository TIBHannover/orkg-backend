package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ClassIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.net.URI
import java.util.UUID

@NodeEntity(label = "Class")
data class Neo4jClass(
    @Id
    @GeneratedValue
    var id: Long? = null
) : Neo4jThing, AuditableEntity() {
    @Property("class_id")
    @Required
    @Convert(ClassIdGraphAttributeConverter::class)
    var classId: ClassId? = null

    @Property("label")
    @Required
    override var label: String? = null

    var uri: String? = null

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)

    constructor(label: String, classId: ClassId, createdBy: UUID = UUID(0, 0)) : this(null) {
        this.label = label
        this.classId = classId
        this.createdBy = createdBy
    }

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        return Class(classId!!, label!!, aURI, createdAt!!, createdBy = createdBy)
    }

    override val thingId: String?
        get() = classId?.value

    override fun toThing() = toClass()
}
