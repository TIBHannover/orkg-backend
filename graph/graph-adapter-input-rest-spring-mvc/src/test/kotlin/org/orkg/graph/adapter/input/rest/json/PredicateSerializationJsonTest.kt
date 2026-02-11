package org.orkg.graph.adapter.input.rest.json

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
import org.orkg.graph.domain.Predicate
import org.orkg.graph.testing.fixtures.createPredicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime

@JsonTest
@ContextConfiguration(classes = [CommonSpringConfig::class, GraphSpringConfig::class])
internal class PredicateSerializationJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var json: JacksonTester<Predicate>

    /**
     * Predicate Representation V1
     *
     * Missing fields:
     * - modifiable
     *
     * Ignored fields:
     * - description
     * - _class
     */
    @Test
    fun `Given a predicate representation v1 serialization, it correctly parses the predicate`() {
        val predicateRepresentationV1 = representationJson("predicates/v1")

        objectMapper.readValue(predicateRepresentationV1, Predicate::class.java).asClue {
            it.id shouldBe ThingId("P30")
            it.label shouldBe "has research field"
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-12T18:58:42.964363+02:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    /**
     * Predicate Representation V2
     *
     * Ignored fields:
     * - description
     * - _class
     */
    @Test
    fun `Given a predicate representation v2 serialization, it correctly parses the predicate`() {
        val predicateRepresentationV2 = representationJson("predicates/v2")

        objectMapper.readValue(predicateRepresentationV2, Predicate::class.java).asClue {
            it.id shouldBe ThingId("P30")
            it.label shouldBe "has research field"
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-12T18:58:42.964363+02:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a predicate domain model serialization, it correctly parses the predicate`() {
        val predicate = modelJson("orkg/predicate")

        objectMapper.readValue(predicate, Predicate::class.java).asClue {
            it.id shouldBe ThingId("P30")
            it.label shouldBe "has research field"
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-12T18:58:42.964363+02:00")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a predicate, it gets correctly serialized to json`() {
        val predicate = createPredicate(
            id = ThingId("P30"),
            label = "has research field",
            createdAt = OffsetDateTime.parse("2021-05-12T18:58:42.964363+02:00"),
            createdBy = ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf"),
            modifiable = true
        )

        val expected = modelJson("orkg/predicate")

        assertThat(json.write(predicate)).isEqualToJson(expected.byteInputStream())
    }
}
