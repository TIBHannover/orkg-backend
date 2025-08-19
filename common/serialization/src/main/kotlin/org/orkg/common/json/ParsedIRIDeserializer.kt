package org.orkg.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.eclipse.rdf4j.common.net.ParsedIRI

class ParsedIRIDeserializer : JsonDeserializer<ParsedIRI>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ParsedIRI? =
        p?.valueAsString?.let(ParsedIRI::create)
}
