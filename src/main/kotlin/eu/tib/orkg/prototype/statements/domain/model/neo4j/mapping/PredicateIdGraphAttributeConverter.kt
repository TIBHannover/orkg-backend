package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.statements.domain.model.*
import org.neo4j.ogm.typeconversion.*

/**
 * Helper class to convert [PredicateId]s for use as a property in a graph.
 */
class PredicateIdGraphAttributeConverter :
    AttributeConverter<PredicateId, String> {
    /**
     * Convert [PredicateId] to graph property.
     */
    override fun toGraphProperty(value: PredicateId?) = "$value"

    /**
     * Convert graph property to [PredicateId].
     */
    override fun toEntityAttribute(value: String?) = PredicateId(value!!)
}
