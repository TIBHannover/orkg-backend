package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import org.neo4j.ogm.typeconversion.AttributeConverter

class PredicateIdConverter :
    AttributeConverter<PredicateId?, String?> {
    override fun toGraphProperty(value: PredicateId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) PredicateId(value) else null
}
