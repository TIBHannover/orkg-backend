package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.domain.SnapshotId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class SnapshotIdSerializer : ValueSerializer<SnapshotId>() {
    override fun serialize(
        value: SnapshotId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
