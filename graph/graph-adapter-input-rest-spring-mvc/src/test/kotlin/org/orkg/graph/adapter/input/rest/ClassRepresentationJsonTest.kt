package org.orkg.graph.adapter.input.rest

import io.mockk.mockk
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.mapping.ClassRepresentationAdapter
import org.orkg.graph.domain.Class
import org.orkg.graph.input.StatementUseCases
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration

/**
 * Test the JSON serialization of a [Class].
 */
@JsonTest
@ContextConfiguration(classes = [ClassRepresentationAdapter::class, CommonJacksonModule::class])
class ClassRepresentationJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<ClassRepresentation>

    private val classRepresentationAdapter: ClassRepresentationAdapter = object : ClassRepresentationAdapter {
        override val statementService: StatementUseCases = mockk()
    }

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
            ).toClassRepresentation("class description")
        }

    private fun serializedClass() = json.write(createClass())
}
