package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.testing.fixtures.Assets.modelJson
import org.orkg.common.testing.fixtures.Assets.representationJson
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.testing.fixtures.createLiteral
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime

@JsonTest
@ContextConfiguration(classes = [CommonSpringConfig::class, GraphSpringConfig::class])
internal class LiteralSerializationJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var json: JacksonTester<Literal>

    /**
     * Literal Representation V1
     *
     * Missing fields:
     * - modifiable
     *
     * Ignored fields:
     * - _class
     */
    @Test
    fun `Given a literal representation v1 serialization, it correctly parses the literal`() {
        val literalRepresentationV1 = representationJson("literals/v1")

        objectMapper.readValue(literalRepresentationV1, Literal::class.java).asClue {
            it.id shouldBe ThingId("L8142")
            it.label shouldBe "value"
            it.datatype shouldBe "xsd:string"
            it.createdAt shouldBe OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    /**
     * Literal Representation V2
     *
     * Ignored fields:
     * - _class
     */
    @Test
    fun `Given a literal representation v2 serialization, it correctly parses the literal`() {
        val literalRepresentationV2 = representationJson("literals/v2")

        objectMapper.readValue(literalRepresentationV2, Literal::class.java).asClue {
            it.id shouldBe ThingId("L8142")
            it.label shouldBe "value"
            it.datatype shouldBe "xsd:string"
            it.createdAt shouldBe OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a literal domain model serialization, it correctly parses the literal`() {
        val literal = modelJson("orkg/literal")

        objectMapper.readValue(literal, Literal::class.java).asClue {
            it.id shouldBe ThingId("L8142")
            it.label shouldBe "value"
            it.datatype shouldBe "xsd:string"
            it.createdAt shouldBe OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a literal, it gets correctly serialized to json`() {
        val literal = createLiteral(
            id = ThingId("L8142"),
            label = "value",
            datatype = Literals.XSD.STRING.prefixedUri,
            createdAt = OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00"),
            createdBy = ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf"),
            modifiable = true
        )

        val expected = modelJson("orkg/literal")

        assertThat(json.write(literal)).isEqualToJson(expected.byteInputStream())
    }
}
