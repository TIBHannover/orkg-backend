package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.testing.fixtures.Assets.modelJson
import org.orkg.common.testing.fixtures.Assets.representationJson
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime

@JsonTest
@ContextConfiguration(classes = [CommonSpringConfig::class, GraphSpringConfig::class])
internal class ResourceSerializationJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var json: JacksonTester<Resource>

    /**
     * Resource Representation V1
     *
     * Missing fields:
     * - formatted_label
     * - featured
     * - unlisted
     * - visibility
     * - verified
     * - modifiable
     *
     * Ignored fields:
     * - shared
     * - formatted_label
     * - _class
     */
    @Test
    fun `Given a resource representation v1 serialization, it correctly parses the resource`() {
        val resourceRepresentationV1 = representationJson("resources/v1")

        objectMapper.readValue(resourceRepresentationV1, Resource::class.java).asClue {
            it.id shouldBe ThingId("R75359")
            it.label shouldBe "Scholarly Knowledge Graphs"
            it.classes shouldBe setOf(ThingId("SmartReview"))
            it.observatoryId shouldBe ObservatoryId("00000000-0000-0000-0000-000000000000")
            it.organizationId shouldBe OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            it.createdAt shouldBe OffsetDateTime.parse("2021-04-28T18:10:09.276644+02:00")
            it.createdBy shouldBe ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2")
            it.visibility shouldBe Visibility.DEFAULT
            it.verified shouldBe null
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.modifiable shouldBe true
        }
    }

    /**
     * Resource Representation V2
     *
     * Missing fields:
     * - visibility
     *
     * Ignored fields:
     * - shared
     * - formatted_label
     * - _class
     */
    @Test
    fun `Given a resource representation v2 serialization, it correctly parses the resource`() {
        val resourceRepresentationV2 = representationJson("resources/v2")

        objectMapper.readValue(resourceRepresentationV2, Resource::class.java).asClue {
            it.id shouldBe ThingId("R0")
            it.label shouldBe "Gruber's design of ontologies"
            it.classes shouldBe setOf(ThingId("Ontology"))
            it.observatoryId shouldBe ObservatoryId("f9008445-b750-436d-96c8-91f62a9be223")
            it.organizationId shouldBe OrganizationId("a6e1ed6f-f6fb-484e-8993-140ab63b12e0")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.visibility shouldBe Visibility.FEATURED
            it.verified shouldBe false
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.modifiable shouldBe true
        }
    }

    /**
     * Resource Representation V3
     *
     * Ignored fields:
     * - shared
     * - formatted_label
     * - _class
     * - featured, if visibility is present
     * - unlisted, if visibility is present
     */
    @Test
    fun `Given a resource representation v3 serialization, it correctly parses the resource`() {
        val resourceRepresentationV3 = representationJson("resources/v3")

        objectMapper.readValue(resourceRepresentationV3, Resource::class.java).asClue {
            it.id shouldBe ThingId("R0")
            it.label shouldBe "Gruber's design of ontologies"
            it.classes shouldBe setOf(ThingId("Ontology"))
            it.observatoryId shouldBe ObservatoryId("f9008445-b750-436d-96c8-91f62a9be223")
            it.organizationId shouldBe OrganizationId("a6e1ed6f-f6fb-484e-8993-140ab63b12e0")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.visibility shouldBe Visibility.FEATURED
            it.verified shouldBe false
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a resource domain model serialization, it correctly parses the resource`() {
        val resource = modelJson("orkg/resource")

        objectMapper.readValue(resource, Resource::class.java).asClue {
            it.id shouldBe ThingId("R0")
            it.label shouldBe "Gruber's design of ontologies"
            it.classes shouldBe setOf(ThingId("Ontology"))
            it.observatoryId shouldBe ObservatoryId("f9008445-b750-436d-96c8-91f62a9be223")
            it.organizationId shouldBe OrganizationId("a6e1ed6f-f6fb-484e-8993-140ab63b12e0")
            it.createdAt shouldBe OffsetDateTime.parse("2019-01-06T15:04:07.692Z")
            it.createdBy shouldBe ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf")
            it.visibility shouldBe Visibility.FEATURED
            it.verified shouldBe false
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.modifiable shouldBe true
        }
    }

    @Test
    fun `Given a resource, it gets correctly serialized to json`() {
        val resource = createResource(
            id = ThingId("R0"),
            label = "Gruber's design of ontologies",
            classes = setOf(ThingId("Ontology")),
            observatoryId = ObservatoryId("f9008445-b750-436d-96c8-91f62a9be223"),
            organizationId = OrganizationId("a6e1ed6f-f6fb-484e-8993-140ab63b12e0"),
            createdAt = OffsetDateTime.parse("2019-01-06T15:04:07.692Z"),
            createdBy = ContributorId("29ed99d5-9135-41e2-8626-2bbd4e6797bf"),
            visibility = Visibility.FEATURED,
            verified = false,
            extractionMethod = ExtractionMethod.UNKNOWN,
            modifiable = true
        )

        val expected = modelJson("orkg/resource")

        assertThat(json.write(resource)).isEqualToJson(expected.byteInputStream())
    }
}
