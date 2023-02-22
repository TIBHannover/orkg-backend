package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.services.toResourceRepresentation
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [Resource].
 */
@JsonTest
class ResourceJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<ResourceRepresentation> // FIXME: This whole test might be pointless.

    @Test
    fun serializedResourceShouldHaveId() {
        assertThat(serializedResource())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("R100")
    }

    @Test
    fun serializedResourceShouldHaveLabel() {
        assertThat(serializedResource())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedResourceShouldHaveCreatedTimestamp() {
        assertThat(serializedResource())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    @Test
    fun serializedResourceShouldHaveClasses() {
        assertThat(serializedResource())
            .extractingJsonPathArrayValue<String>("@.classes")
            .containsExactlyInAnyOrder("C1", "C2", "C3")
    }

    @Test
    fun serializedResourceShouldHaveSharedProperty() {
        assertThat(serializedResource())
            .extractingJsonPathNumberValue("@.shared")
            .isEqualTo(11)
    }

    @Test
    fun serializedOfNormalResourceShouldHaveNullFormattedLabel() {
        assertThat(serializedResource())
            .extractingJsonPathStringValue("@.formatted_label")
            .isNull()
    }

    private fun createResource() =
        Resource(
            ThingId("R100"),
            "label",
            OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
            setOf(ThingId("C1"), ThingId("C2"), ThingId("C3"))
        ).toResourceRepresentation(
            mapOf(ThingId("R100") to 11),
            emptyMap()
        )

    private fun serializedResource() = json.write(createResource())
}
