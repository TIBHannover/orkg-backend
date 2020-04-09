package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * Test the JSON serialization of a [Literal].
 */
@JsonTest
class LiteralJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<Literal>

    @Test
    fun serializedLiteralShouldHaveId() {
        assertThat(serializedLiteral())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("L100")
    }

    @Test
    fun serializedLiteralShouldHaveLabel() {
        assertThat(serializedLiteral())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedLiteralShouldHaveCreatedTimestamp() {
        assertThat(serializedLiteral())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    @Test
    fun serializedLiteralShouldHaveDatatype() {
        assertThat(serializedLiteral())
            .extractingJsonPathStringValue("@.datatype")
            .isEqualTo("xs:string")
    }

    @Test
    fun serializedLiteralWithCustomDatatypeShouldHaveCustomDatatype() {
        assertThat(json.write(createLiteral().copy(datatype = "xs:number")))
            .extractingJsonPathStringValue("@.datatype")
            .isEqualTo("xs:number")
    }

    private fun createLiteral() =
        Literal(
            id = LiteralId(100),
            label = "label",
            createdAt = OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3))
        )

    private fun serializedLiteral() = json.write(createLiteral())
}
