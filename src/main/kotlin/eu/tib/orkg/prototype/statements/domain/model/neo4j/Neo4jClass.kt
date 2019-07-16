package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ClassIdGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.net.URI

@NodeEntity(label = "Class")
data class Neo4jClass(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @Property("class_id")
    @Required
    @Convert(ClassIdGraphAttributeConverter::class)
    var classId: ClassId? = null

    @Property("label")
    @Required
    var label: String? = null

    var uri: String? = null

    constructor(label: String, classId: ClassId) : this(null) {
        this.label = label
        this.classId = classId
    }

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        return Class(classId!!, label!!, aURI)
    }
}
