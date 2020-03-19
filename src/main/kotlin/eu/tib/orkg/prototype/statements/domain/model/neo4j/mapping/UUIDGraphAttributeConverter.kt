package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import java.util.UUID
import org.neo4j.ogm.typeconversion.AttributeConverter

/**
 * Helper class to convert [UUID]s for use as a property in a graph.
 */
class UUIDGraphAttributeConverter :
    AttributeConverter<UUID, String> {
    /**
     * Convert [UUID] to graph property.
     */
    override fun toGraphProperty(value: UUID?): String? = "$value"

    /**
     * Convert graph property to [UUID].
     */
    override fun toEntityAttribute(value: String?): UUID? = UUID.fromString(value)
}
