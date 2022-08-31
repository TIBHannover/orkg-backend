package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.neo4j.ogm.typeconversion.AttributeConverter

class ClassIdConverter :
    AttributeConverter<ClassId?, String?> {
    override fun toGraphProperty(value: ClassId?) = if (value != null) "$value" else null

    override fun toEntityAttribute(value: String?) =
        if (value != null && value.isNotBlank()) ClassId(value) else null
}
