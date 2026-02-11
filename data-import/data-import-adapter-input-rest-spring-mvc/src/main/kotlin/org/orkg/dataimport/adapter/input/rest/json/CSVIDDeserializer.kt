package org.orkg.dataimport.adapter.input.rest.json

import org.orkg.dataimport.domain.csv.CSVID
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class CSVIDDeserializer : ValueDeserializer<CSVID>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CSVID? =
        p?.valueAsString?.let(::CSVID)
}
