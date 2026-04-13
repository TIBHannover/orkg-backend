package org.orkg.common.json

import org.orkg.common.IRI
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class IRIDeserializer : ValueDeserializer<IRI>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): IRI? =
        p?.valueAsString?.let(IRI::create)
}
