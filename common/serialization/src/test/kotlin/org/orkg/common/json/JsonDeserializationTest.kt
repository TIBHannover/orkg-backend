package org.orkg.common.json

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [CommonSpringConfig::class])
internal class JsonDeserializationTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun successfullyParsesStringToObject() {
        objectMapper.readValue("""{"field": "foo", "list": ["bar"]}""", JsonObject::class.java).asClue {
            it.field shouldBe "foo"
            it.list.single() shouldBe "bar"
        }
    }

    @Test
    fun throwsErrorWhenNonNullValueIsNull() {
        shouldThrow<JsonMappingException> {
            objectMapper.readValue("""{"field": null, "list": []}""", JsonObject::class.java)
        }
    }

    @Test
    fun throwsErrorWhenNullValueIsInNonNullList() {
        shouldThrow<JsonMappingException> {
            objectMapper.readValue("""{"field": "foo", "list": [null]}""", JsonObject::class.java)
        }
    }

    data class JsonObject(
        val field: String,
        val list: List<String>
    )
}
