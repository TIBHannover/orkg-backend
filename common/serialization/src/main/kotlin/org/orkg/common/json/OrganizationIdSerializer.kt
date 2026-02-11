package org.orkg.common.json

import org.orkg.common.OrganizationId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class OrganizationIdSerializer : ValueSerializer<OrganizationId>() {
    override fun serialize(
        value: OrganizationId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
