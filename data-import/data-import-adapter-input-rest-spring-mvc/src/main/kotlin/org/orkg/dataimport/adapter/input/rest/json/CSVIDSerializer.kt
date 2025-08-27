package org.orkg.dataimport.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.dataimport.domain.csv.CSVID

class CSVIDSerializer : JsonSerializer<CSVID>() {
    override fun serialize(value: CSVID?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value.toString())
    }
}
