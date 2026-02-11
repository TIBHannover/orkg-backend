package org.orkg.common.json

import org.orkg.common.ContributorId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ContributorIdSerializer : ValueSerializer<ContributorId>() {
    override fun serialize(
        value: ContributorId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
