package org.orkg.common.json

import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.InvalidUUID
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class OrganizationIdDeserializer : ValueDeserializer<OrganizationId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): OrganizationId? =
        p?.valueAsString?.let {
            try {
                OrganizationId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
