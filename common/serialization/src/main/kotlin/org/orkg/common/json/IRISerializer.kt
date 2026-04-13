package org.orkg.common.json

import org.orkg.common.IRI
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class IRISerializer : ValueSerializer<IRI>() {
    override fun serialize(value: IRI?, gen: JsonGenerator?, serializers: SerializationContext?) {
        gen?.writeString(value.toString())
    }
}
