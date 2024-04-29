package org.orkg.common.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.common.OrganizationId

class OrganizationIdSerializer : JsonSerializer<OrganizationId>() {
    override fun serialize(
        value: OrganizationId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
