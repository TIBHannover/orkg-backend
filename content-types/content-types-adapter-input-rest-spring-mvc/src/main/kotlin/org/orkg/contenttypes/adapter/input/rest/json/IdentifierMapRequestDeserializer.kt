package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.adapter.input.rest.IdentifierMapRequest
import tools.jackson.core.JsonParser
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.exc.MismatchedInputException

class IdentifierMapRequestDeserializer : ValueDeserializer<IdentifierMapRequest>() {
    private val typeReference = object : TypeReference<Map<String, List<String?>>>() {}

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): IdentifierMapRequest {
        val value = ctxt.readValue(p, typeReference).mapValues { (key, value) ->
            value.mapIndexed { index, it ->
                it ?: throw MismatchedInputException.from(
                    p,
                    String::class.java,
                    """Field "${fieldPath(p, key, index)}" is either missing, "null", of invalid type, or contains "null" values."""
                )
            }
        }
        return IdentifierMapRequest(value)
    }

    private fun fieldPath(jsonParser: JsonParser, key: String, index: Int): String =
        "$." + jsonParser.currentName()?.let { "$it." }.orEmpty() + "$key[$index]"
}
