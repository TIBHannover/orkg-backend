package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.ClassRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [Class].
 */
@JsonTest
class ClassJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<ClassRepresentation>

    private val classRepresentationAdapter: ClassRepresentationAdapter = object : ClassRepresentationAdapter {}

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
        with(classRepresentationAdapter) {
            Class(
                ThingId("C100"),
                "label",
                URI("http://example.org/path/to/file#with-fragment"),
                OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3))
            ).toClassRepresentation()
        }

    private fun serializedClass() = json.write(createClass())
}
