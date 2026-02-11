package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.testing.fixtures.Assets.modelJson
import org.orkg.common.testing.fixtures.Assets.representationJson
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.graph.domain.Class
import org.orkg.graph.testing.fixtures.createClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.Jackson2Tester
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime

@JsonTest
@ContextConfiguration(classes = [CommonSpringConfig::class, GraphSpringConfig::class])
internal class ClassSerializationJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var json: Jackson2Tester<Class>

    /**
     * Class Representation V1
     *
     * Missing fields:
     * - modifiable
     *
     * Ignored fields:
     * - description
     * - _class
     */
    @Test
    fun `Given a class representation v1 serialization, it correctly parses the class`() {
        val classRepresentationV1 = representationJson("classes/v1")

        objectMapper.readValue(classRepresentationV1, Class::class.java).asClue {
            it.id shouldBe ThingId("C0")
            it.label shouldBe "qb:Observation"
            it.uri shouldBe ParsedIRI.create("http://purl.org/linked-data/cube#Observation")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    /**
     * Class Representation V2
     *
     * Ignored fields:
     * - description
     * - _class
     */
    @Test
    fun `Given a class representation v2 serialization, it correctly parses the class`() {
        val classRepresentationV2 = representationJson("classes/v2")

        objectMapper.readValue(classRepresentationV2, Class::class.java).asClue {
            it.id shouldBe ThingId("C0")
            it.label shouldBe "qb:Observation"
            it.uri shouldBe ParsedIRI.create("http://purl.org/linked-data/cube#Observation")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a class domain model serialization, it correctly parses the class`() {
        val `class` = modelJson("orkg/class")

        objectMapper.readValue(`class`, Class::class.java).asClue {
            it.id shouldBe ThingId("C0")
            it.label shouldBe "qb:Observation"
            it.uri shouldBe ParsedIRI.create("http://purl.org/linked-data/cube#Observation")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a class, it gets correctly serialized to json`() {
        val `class` = createClass(
            id = ThingId("C0"),
            label = "qb:Observation",
            uri = ParsedIRI.create("http://purl.org/linked-data/cube#Observation"),
            createdAt = OffsetDateTime.parse("2019-01-06T15:04:07.692Z"),
            createdBy = ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf"),
            modifiable = true
        )

        val expected = modelJson("orkg/class")

        assertThat(json.write(`class`)).isEqualToJson(expected.byteInputStream())
    }
}
