package org.orkg.common.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.eclipse.rdf4j.common.net.ParsedIRI

class ParsedIRISerializer : JsonSerializer<ParsedIRI>() {
    override fun serialize(value: ParsedIRI?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value.toString())
    }
}
