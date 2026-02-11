package org.orkg.common.json

import org.orkg.common.ContributorId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ContributorIdDeserializer : ValueDeserializer<ContributorId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ContributorId? =
        p?.valueAsString?.let {
            ContributorId(UUID.fromString(it))
        }
}
