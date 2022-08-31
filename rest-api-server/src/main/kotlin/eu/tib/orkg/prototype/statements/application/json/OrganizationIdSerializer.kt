package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId

class OrganizationIdSerializer : JsonSerializer<OrganizationId>() {
    override fun serialize(
        value: OrganizationId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
