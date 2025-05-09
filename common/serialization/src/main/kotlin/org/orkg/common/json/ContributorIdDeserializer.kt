package org.orkg.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.common.ContributorId
import java.util.UUID

class ContributorIdDeserializer : JsonDeserializer<ContributorId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ContributorId? =
        p?.valueAsString?.let {
            ContributorId(UUID.fromString(it))
        }
}
