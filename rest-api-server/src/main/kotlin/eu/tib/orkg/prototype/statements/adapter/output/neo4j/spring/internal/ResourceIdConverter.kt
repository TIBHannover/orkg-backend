package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.neo4j.ogm.typeconversion.AttributeConverter

class ResourceIdConverter :
    AttributeConverter<ResourceId?, String?> {
    override fun toGraphProperty(value: ResourceId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ResourceId(value) else null
}
