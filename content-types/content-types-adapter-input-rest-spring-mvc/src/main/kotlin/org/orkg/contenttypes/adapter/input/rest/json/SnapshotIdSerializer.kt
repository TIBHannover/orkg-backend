package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.contenttypes.domain.SnapshotId

class SnapshotIdSerializer : JsonSerializer<SnapshotId>() {
    override fun serialize(
        value: SnapshotId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value.toString())
    }
}
