package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import java.net.URI
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Class")
data class Neo4jClass(
    @Id
    @GeneratedValue
    var id: Long? = null,
    @Relationship(type = "RELATED", direction = Relationship.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jClass> = mutableSetOf()
) : Neo4jThing, AuditableEntity() {
    @Property("class_id")
    @Required
    @Convert(ClassIdConverter::class)
    var classId: ClassId? = null

    @Property("label")
    @Required
    override var label: String? = null

    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    constructor(label: String, classId: ClassId, createdBy: ContributorId = ContributorId.createUnknownContributor(), uri: URI?) : this(null) {
        this.label = label
        this.classId = classId
        this.uri = uri?.toString()
        this.createdBy = createdBy
    }

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        val clazz = Class(
            id = classId!!,
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
