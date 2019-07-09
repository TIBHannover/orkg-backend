package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.neo4j.ogm.typeconversion.AttributeConverter

/**
 * Helper class to convert [StatementId]s for use as a property in a graph.
 */
class StatementIdGraphAttributeConverter :
    AttributeConverter<StatementId, String> {
    /**
     * Convert [StatementId] to graph property.
     */
    override fun toGraphProperty(value: StatementId?) = "$value"

    /**
     * Convert graph property to [StatementId].
     */
    override fun toEntityAttribute(value: String?) = StatementId(value!!)
}
