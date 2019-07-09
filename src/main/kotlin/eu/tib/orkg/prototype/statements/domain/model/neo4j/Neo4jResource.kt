package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceObject
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ResourceIdGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Resource")
data class Neo4jResource(
    @Id
    @GeneratedValue
    var id: Long? = null
) {

    @Property("label")
    @Required
    var label: String? = null

    @Property("resource_id")
    @Required
    @Convert(ResourceIdGraphAttributeConverter::class)
    var resourceId: ResourceId? = null

    @Relationship(type = "RELATES_TO")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithResource> = mutableSetOf()

    constructor(label: String, resourceId: ResourceId) : this(null) {
        this.label = label
        this.resourceId = resourceId
    }

    fun toResource() = Resource(resourceId, label!!)

    fun toObject() = ResourceObject(resourceId, label!!)
}
