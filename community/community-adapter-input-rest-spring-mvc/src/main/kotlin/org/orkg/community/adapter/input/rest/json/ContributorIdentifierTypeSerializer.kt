package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.community.domain.ContributorIdentifier

class ContributorIdentifierTypeSerializer : JsonSerializer<ContributorIdentifier.Type>() {
    override fun serialize(
        value: ContributorIdentifier.Type?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value?.id)
    }
}
