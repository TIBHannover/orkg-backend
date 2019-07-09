package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import org.neo4j.ogm.typeconversion.AttributeConverter

/**
 * Helper class to convert [LiteralId]s for use as a property in a graph.
 */
class LiteralIdGraphAttributeConverter :
    AttributeConverter<LiteralId, String> {
    /**
     * Convert [LiteralId] to graph property.
     */
    override fun toGraphProperty(value: LiteralId?) = "$value"

    /**
     * Convert graph property to [LiteralId].
     */
    override fun toEntityAttribute(value: String?) = LiteralId(value!!)
}
