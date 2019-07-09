package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.neo4j.ogm.typeconversion.AttributeConverter

/**
 * Helper class to convert [ResourceId]s for use as a property in a graph.
 */
class ResourceIdGraphAttributeConverter :
    AttributeConverter<ResourceId, String> {
    /**
     * Convert [ResourceId] to graph property.
     */
    override fun toGraphProperty(value: ResourceId?) = "$value"

    /**
     * Convert graph property to [ResourceId].
     */
    override fun toEntityAttribute(value: String?) = ResourceId(value!!)
}
