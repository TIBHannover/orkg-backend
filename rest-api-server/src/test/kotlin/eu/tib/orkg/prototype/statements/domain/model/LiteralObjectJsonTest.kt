package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [LiteralObject].
 */
@JsonTest
class LiteralObjectJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<LiteralObject>

    @Test
    fun serializedLiteralObjectShouldHaveId() {
        assertThat(serializedLiteralObject())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("L100")
    }

    @Test
    fun serializedLiteralObjectShouldHaveLabel() {
        assertThat(serializedLiteralObject())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedLiteralObjectShouldHaveCreatedTimestamp() {
        assertThat(serializedLiteralObject())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    @Test
    fun serializedLiteralObjectShouldHaveClasses() {
        assertThat(serializedLiteralObject())
            .extractingJsonPathArrayValue<String>("@.classes")
            .containsExactlyInAnyOrder("C1", "C2", "C3")
    }

    @Test
    fun serializedLiteralObjectShouldHaveClassProperty() {
        assertThat(serializedLiteralObject())
            .extractingJsonPathStringValue("@._class")
            .isEqualTo("literal")
    }

    private fun createLiteralObject() =
        LiteralObject(
            LiteralId(100),
            "label",
            OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
            setOf(ThingId("C1"), ThingId("C2"), ThingId("C3"))
        )

    private fun serializedLiteralObject() = json.write(createLiteralObject())
}
