package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [Literal].
 */
@JsonTest
@DisplayName("Literal JSON Serialization Tests")
class LiteralJsonTest {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection") // Bug in IDEA, works fine.
    @Autowired
    private lateinit var json: JacksonTester<Literal>

    @Nested
    @DisplayName("Given a Literal with default values")
    inner class GivenALiteralWithDefaultValues {

        private val serializedLiteral = createLiteral().serialize()

        @Test
        @DisplayName("then the data type should be `xs:string`")
        fun thenTheDataTypeShouldBeString() {
            assertThat(serializedLiteral)
                .extractingJsonPathStringValue("@.datatype")
                .isEqualTo("xs:string")
        }

        @Test
        @DisplayName("then the timestamp should be serialized to ISO-8601")
        fun serializedLiteralShouldHaveCreatedTimestamp() {
            // Technically not the default value because it is constructed, but we have no control over the clock.
            assertThat(serializedLiteral)
                .extractingJsonPathStringValue("@.created_at")
                .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
        }
    }

    @Nested
    @DisplayName("Given a Literal with provided values")
    inner class GivenALiteralWithProvidedValues {

        private val serializedLiteral = createLiteral().copy(datatype = "xs:number").serialize()

        @Test
        @DisplayName("then the ID should be the same")
        fun thenTheIdShouldBeTheSame() {
            assertThat(serializedLiteral)
                .extractingJsonPathStringValue("@.id")
                .isEqualTo("L100")
        }

        @Test
        @DisplayName("then the label should be the same")
        fun thenTheLabelShouldBeTheSame() {
            assertThat(serializedLiteral)
                .extractingJsonPathStringValue("@.label")
                .isEqualTo("label")
        }

        @Test
        @DisplayName("then the data type should be the same")
        fun thenTheDataTypeShouldBeTheSame() {
            assertThat(serializedLiteral)
                .extractingJsonPathStringValue("@.datatype")
                .isEqualTo("xs:number")
        }
    }

    // TODO: Test that "_class" cannot be set or changed

    private fun createLiteral() =
        Literal(
            id = LiteralId(100),
            label = "label",
            createdAt = OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3))
        )

    private fun Literal.serialize() = json.write(this)
}
