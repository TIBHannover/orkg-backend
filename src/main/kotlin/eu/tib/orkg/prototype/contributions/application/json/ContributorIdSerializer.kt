package eu.tib.orkg.prototype.contributions.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId

class ContributorIdSerializer : JsonSerializer<ContributorId>() {
    override fun serialize(
        value: ContributorId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
