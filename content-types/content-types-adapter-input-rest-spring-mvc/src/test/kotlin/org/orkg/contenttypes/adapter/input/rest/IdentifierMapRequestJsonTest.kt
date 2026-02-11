package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.contenttypes.adapter.input.rest.configuration.ContentTypeSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.exc.MismatchedInputException

@WebMvcTest
@ContextConfiguration(classes = [CommonSpringConfig::class, ContentTypeSpringConfig::class])
internal class IdentifierMapRequestJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun successfullyParsesStringToIdentifierMapRequest() {
        objectMapper.readValue("""{"field": ["foo"], "list": ["bar"]}""", IdentifierMapRequest::class.java).asClue {
            it shouldBe IdentifierMapRequest(mapOf("field" to listOf("foo"), "list" to listOf("bar")))
        }
    }

    @Test
    fun successfullyParsesStringToIdentifierMapRequestWhenNested() {
        objectMapper.readValue("""{"wrapper":{"field": ["foo"], "list": ["bar"]}}""", object : TypeReference<Map<String, IdentifierMapRequest>>() {}).asClue {
            it shouldBe mapOf("wrapper" to IdentifierMapRequest(mapOf("field" to listOf("foo"), "list" to listOf("bar"))))
        }
    }

    @Test
    fun throwsErrorWhenListValueIsNull() {
        assertThrows<MismatchedInputException> {
            objectMapper.readValue("""{"field": ["foo"], "list": ["bar", null]}""", IdentifierMapRequest::class.java)
        }.asClue {
            it.originalMessage shouldBe """Field "$.list[1]" is either missing, "null", of invalid type, or contains "null" values."""
        }
    }

    @Test
    fun throwsErrorWhenListValueIsNullAndObjectIsNested() {
        assertThrows<MismatchedInputException> {
            objectMapper.readValue("""{"wrapper":{"field": ["foo"], "list": ["bar", null]}}""", object : TypeReference<Map<String, IdentifierMapRequest>>() {})
        }.asClue {
            it.originalMessage shouldBe """Field "$.wrapper.list[1]" is either missing, "null", of invalid type, or contains "null" values."""
        }
    }
}
