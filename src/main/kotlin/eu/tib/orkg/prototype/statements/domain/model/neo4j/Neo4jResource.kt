package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceObject
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ResourceIdGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Labels
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
) : AuditableEntity() {

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

    @Relationship(type = "RELATES_TO", direction = Relationship.INCOMING)
    @JsonIgnore
    var objectOf: MutableSet<Neo4jStatementWithResource> = mutableSetOf()

    /**
     * List of node labels. Labels other than the `Resource` label are mapped to classes.
     */
    @Labels
    private var labels: MutableList<String> = mutableListOf()

    /**
     * The list of classes that this node belongs to.
     */
    val classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()

    constructor(label: String, resourceId: ResourceId) : this(null) {
        this.label = label
        this.resourceId = resourceId
    }

    fun toResource() = Resource(resourceId, label!!, createdAt, classes)

    fun toObject() = ResourceObject(resourceId, label!!, createdAt, classes)

    /**
     * Assign a class to this `Resource` node.
     */
    fun assignTo(clazz: String) = labels.add(clazz)
}
