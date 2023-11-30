package org.orkg.graph.adapter.input.rest

import io.mockk.mockk
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration

/**
 * Test the JSON serialization of a [Resource].
 */
@JsonTest
@ContextConfiguration(classes = [ResourceRepresentationAdapter::class, CommonJacksonModule::class])
class ResourceRepresentationJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<ResourceRepresentation> // FIXME: This whole test might be pointless.

    private val resourceRepresentationAdapter: ResourceRepresentationAdapter = object : ResourceRepresentationAdapter {
        override val statementService: StatementUseCases = mockk()
        override val formattedLabelRepository: FormattedLabelRepository = mockk()
        override val flags: FeatureFlagService = mockk()
    }

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
        with(resourceRepresentationAdapter) {
            Resource(
                ThingId("R100"),
                "label",
                OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3)),
                setOf(ThingId("C1"), ThingId("C2"), ThingId("C3"))
            ).toResourceRepresentation(
                mapOf(ThingId("R100") to 11),
                emptyMap()
            )
        }

    private fun serializedResource() = json.write(createResource())
}
