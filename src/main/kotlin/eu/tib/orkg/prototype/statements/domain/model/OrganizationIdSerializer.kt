package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class OrganizationIdSerializer : JsonSerializer<OrganizationId>() {
    override fun serialize(
        value: OrganizationId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
