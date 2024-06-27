package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class

class ClassDeserializer : JsonDeserializer<Class>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): Class = with(p!!.codec.readTree<JsonNode>(p)) {
        Class(
            id = ThingId(this["id"].asText()),
            label = this["label"].asText(),
            uri = this["uri"]?.textValue()?.let(URI::create),
            createdAt = OffsetDateTime.parse(this["created_at"].asText()),
            createdBy = ContributorId(this["created_by"].asText())
        )
    }
}
