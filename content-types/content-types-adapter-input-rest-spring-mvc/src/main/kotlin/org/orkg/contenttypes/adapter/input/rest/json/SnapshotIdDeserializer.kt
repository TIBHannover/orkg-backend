package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.domain.SnapshotId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class SnapshotIdDeserializer : ValueDeserializer<SnapshotId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): SnapshotId? =
        p?.valueAsString?.let(::SnapshotId)
}
