package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.neo4j.ogm.typeconversion.AttributeConverter

class ThingIdConverter : AttributeConverter<ThingId?, String?> {
    override fun toGraphProperty(value: ThingId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (!value.isNullOrBlank()) ThingId(value) else null
}
