package org.orkg.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.InvalidUUID
import java.util.UUID

class OrganizationIdDeserializer : JsonDeserializer<OrganizationId>() {
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
