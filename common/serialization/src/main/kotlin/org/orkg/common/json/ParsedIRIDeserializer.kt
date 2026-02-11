package org.orkg.common.json

import org.eclipse.rdf4j.common.net.ParsedIRI
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class ParsedIRIDeserializer : ValueDeserializer<ParsedIRI>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ParsedIRI? =
        p?.valueAsString?.let(ParsedIRI::create)
}
