package org.orkg.common.json

import org.eclipse.rdf4j.common.net.ParsedIRI
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ParsedIRISerializer : ValueSerializer<ParsedIRI>() {
    override fun serialize(value: ParsedIRI?, gen: JsonGenerator?, serializers: SerializationContext?) {
        gen?.writeString(value.toString())
    }
}
