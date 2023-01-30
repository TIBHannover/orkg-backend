package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [ResourceObject].
 */
@JsonTest
class ResourceObjectJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<ResourceObject>

    @Test
    fun serializedResourceObjectShouldHaveId() {
        assertThat(serializedResourceObject())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("R100")
    }

    @Test
    fun serializedResourceObjectShouldHaveLabel() {
        assertThat(serializedResourceObject())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedResourceObjectShouldHaveCreatedTimestamp() {
        assertThat(serializedResourceObject())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    @Test
    fun serializedResourceObjectShouldHaveClasses() {
        assertThat(serializedResourceObject())
            .extractingJsonPathArrayValue<String>("@.classes")
            .containsExactlyInAnyOrder("C1", "C2", "C3")
    }

    @Test
    fun serializedResourceObjectShouldHaveClassProperty() {
        assertThat(serializedResourceObject())
            .extractingJsonPathStringValue("@._class")
            .isEqualTo("resource")
    }

    private fun createResourceObject() =
        ResourceObject(
            ResourceId(100),
            "label",
            OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
            setOf(ThingId("C1"), ThingId("C2"), ThingId("C3"))
        )

    private fun serializedResourceObject() = json.write(createResourceObject())
}
