package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.contenttypes.domain.SnapshotId

class SnapshotIdDeserializer : JsonDeserializer<SnapshotId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): SnapshotId? =
        p?.valueAsString?.let(::SnapshotId)
}
