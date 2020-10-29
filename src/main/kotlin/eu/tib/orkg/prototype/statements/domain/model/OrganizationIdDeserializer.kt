package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.UUID

class OrganizationIdDeserializer :
    JsonDeserializer<OrganizationId>() {

        override fun deserialize(
            p: JsonParser?,
            ctxt: DeserializationContext?
        ): OrganizationId? =
            p?.valueAsString?.let {
                OrganizationId(UUID.fromString(it))
            }
}
