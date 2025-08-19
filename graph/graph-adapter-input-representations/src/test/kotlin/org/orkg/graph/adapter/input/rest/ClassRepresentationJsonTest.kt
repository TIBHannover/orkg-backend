package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.mapping.ClassRepresentationAdapter
import org.orkg.graph.domain.Class
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Test the JSON serialization of a [Class].
 */
@JsonTest
@ContextConfiguration(classes = [ClassRepresentationAdapter::class, CommonJacksonModule::class])
internal class ClassRepresentationJsonTest {
    @Autowired
    private lateinit var json: JacksonTester<ClassRepresentation>

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
            .isEqualTo("https://example.org/path/to/file#with-fragment")
    }

    @Test
    fun serializedClassShouldHaveCreatedTimestamp() {
        assertThat(serializedClass())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    private fun createClass() =
        ClassRepresentation(
            id = ThingId("C100"),
            label = "label",
            description = "class description",
            uri = ParsedIRI.create("https://example.org/path/to/file#with-fragment"),
            createdAt = OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
            createdBy = ContributorId.UNKNOWN,
            modifiable = true
        )

    private fun serializedClass() = json.write(createClass())
}
