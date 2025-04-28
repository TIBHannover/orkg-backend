package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.UnknownIdentifierType

class ContributorIdentifierTypeDeserializer : JsonDeserializer<ContributorIdentifier.Type>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ContributorIdentifier.Type? =
        p?.valueAsString?.let {
            ContributorIdentifier.Type.byId(it.lowercase()) ?: throw UnknownIdentifierType(it)
        }
}
