package org.orkg.community.adapter.input.rest.json

import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.UnknownIdentifierType
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class ContributorIdentifierTypeDeserializer : ValueDeserializer<ContributorIdentifier.Type>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ContributorIdentifier.Type? =
        p?.valueAsString?.let {
            ContributorIdentifier.Type.byId(it.lowercase()) ?: throw UnknownIdentifierType(it)
        }
}
