package org.orkg.dataimport.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.dataimport.domain.csv.CSVID

class CSVIDDeserializer : JsonDeserializer<CSVID>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CSVID? =
        p?.valueAsString?.let(::CSVID)
}
