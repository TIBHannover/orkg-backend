package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Test the JSON serialization of a [Class].
 */
@JsonTest
class ClassJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<Class>

    @Test
    fun serializedClassShouldHaveId() {
        assertThat(serializedClass())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("C100")
    }

    @Test
    fun serializedClassShouldHaveLabel() {
        assertThat(serializedClass())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedClassShouldHaveURI() {
        assertThat(serializedClass())
            .extractingJsonPathStringValue("@.uri")
            .isEqualTo("http://example.org/path/to/file#with-fragment")
    }

    @Test
    fun serializedClassShouldHaveCreatedTimestamp() {
        assertThat(serializedClass())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    private fun createClass() =
        Class(
            ClassId(100),
            "label",
            URI("http://example.org/path/to/file#with-fragment"),
            OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
            null
        )

    private fun serializedClass() = json.write(createClass())
}
