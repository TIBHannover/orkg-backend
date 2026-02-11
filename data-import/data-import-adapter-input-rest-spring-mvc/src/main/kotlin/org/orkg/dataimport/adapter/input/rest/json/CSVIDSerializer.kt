package org.orkg.dataimport.adapter.input.rest.json

import org.orkg.dataimport.domain.csv.CSVID
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class CSVIDSerializer : ValueSerializer<CSVID>() {
    override fun serialize(value: CSVID?, gen: JsonGenerator?, serializers: SerializationContext?) {
        gen?.writeString(value.toString())
    }
}
