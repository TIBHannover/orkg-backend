package org.orkg.community.adapter.input.rest.json

import org.orkg.community.domain.ContributorIdentifier
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ContributorIdentifierTypeSerializer : ValueSerializer<ContributorIdentifier.Type>() {
    override fun serialize(
        value: ContributorIdentifier.Type?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value?.id)
    }
}
