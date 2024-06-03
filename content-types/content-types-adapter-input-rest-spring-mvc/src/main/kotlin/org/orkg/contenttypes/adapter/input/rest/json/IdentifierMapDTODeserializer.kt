package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.contenttypes.adapter.input.rest.IdentifierMapDTO

class IdentifierMapDTODeserializer : JsonDeserializer<IdentifierMapDTO>() {
    private val typeReference = object : TypeReference<Map<String, List<String?>>>() {}

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): IdentifierMapDTO {
        val value = p.codec.readValue(p, typeReference).mapValues { (key, value) ->
            value.mapIndexed { index, it ->
                it ?: throw JsonParseException(
                    """Field "${fieldPath(p, key, index)}" is either missing, "null", of invalid type, or contains "null" values."""
                )
            }
        }
        return IdentifierMapDTO(value)
    }

    private fun fieldPath(jsonParser: JsonParser, key: String, index: Int): String =
        "$." + jsonParser.currentName?.let { "$it." }.orEmpty() + "$key[$index]"
}
