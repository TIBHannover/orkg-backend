package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.contenttypes.adapter.input.rest.configuration.ContentTypeSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [CommonSpringConfig::class, ContentTypeSpringConfig::class])
internal class IdentifierMapDTOJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun successfullyParsesStringToIdentifierMapDTO() {
        objectMapper.readValue("""{"field": ["foo"], "list": ["bar"]}""", IdentifierMapDTO::class.java).asClue {
            it shouldBe IdentifierMapDTO(mapOf("field" to listOf("foo"), "list" to listOf("bar")))
        }
    }

    @Test
    fun successfullyParsesStringToIdentifierMapDTOWhenNested() {
        objectMapper.readValue("""{"wrapper":{"field": ["foo"], "list": ["bar"]}}""", object : TypeReference<Map<String, IdentifierMapDTO>>() {}).asClue {
            it shouldBe mapOf("wrapper" to IdentifierMapDTO(mapOf("field" to listOf("foo"), "list" to listOf("bar"))))
        }
    }

    @Test
    fun throwsErrorWhenListValueIsNull() {
        assertThrows<JsonParseException> {
            objectMapper.readValue("""{"field": ["foo"], "list": ["bar", null]}""", IdentifierMapDTO::class.java)
        }.asClue {
            it.originalMessage shouldBe """Field "$.list[1]" is either missing, "null", of invalid type, or contains "null" values."""
        }
    }

    @Test
    fun throwsErrorWhenListValueIsNullAndObjectIsNested() {
        assertThrows<JsonParseException> {
            objectMapper.readValue("""{"wrapper":{"field": ["foo"], "list": ["bar", null]}}""", object : TypeReference<Map<String, IdentifierMapDTO>>() {})
        }.asClue {
            it.originalMessage shouldBe """Field "$.wrapper.list[1]" is either missing, "null", of invalid type, or contains "null" values."""
        }
    }
}
