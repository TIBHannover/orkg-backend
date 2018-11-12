package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.*
import eu.tib.orkg.prototype.statements.domain.model.*
import org.neo4j.ogm.annotation.*

@NodeEntity(label = "Resource")
data class Neo4jResource(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @Property("label")
    @Required
    var label: String? = null

    @Relationship(type = "RELATES_TO")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithResource> = mutableSetOf()

    constructor(id: Long? = null, label: String) : this(id) {
        this.label = label
    }

    fun toResource(): Resource {
        // Use vals to protect values from being modified.
        val id = id
        val label = label

        if (id == null || label == null)
            throw IllegalStateException("This should never happen!")

        return Resource(ResourceId(id), label = label)
    }
}
