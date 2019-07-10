package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.neo4j.ogm.typeconversion.AttributeConverter

/**
 * Helper class to convert [ClassId]s for use as a property in a graph.
 */
class ClassIdGraphAttributeConverter :
    AttributeConverter<ClassId, String> {
    /**
     * Convert [ClassId] to graph property.
     */
    override fun toGraphProperty(value: ClassId?) = "$value"

    /**
     * Convert graph property to [ClassId].
     */
    override fun toEntityAttribute(value: String?) = ClassId(value!!)
}
