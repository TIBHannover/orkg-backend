package eu.tib.orkg.prototype.contributions.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.UUID

class ContributorIdDeserializer :
    JsonDeserializer<ContributorId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ContributorId? =
        p?.valueAsString?.let {
            ContributorId(UUID.fromString(it))
        }
}
