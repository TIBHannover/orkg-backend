package org.orkg.graph.adapter.input.rest

import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.mapping.PredicateRepresentationAdapter
import org.orkg.graph.domain.Predicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ContextConfiguration

/**
 * Test the JSON serialization of a [Predicate].
 */
@JsonTest
@ContextConfiguration(classes = [PredicateRepresentationAdapter::class, CommonJacksonModule::class])
class PredicateRepresentationJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<PredicateRepresentation>

    private val predicateRepresentationAdapter: PredicateRepresentationAdapter =
        object : PredicateRepresentationAdapter {}

    @Test
    fun serializedPredicateShouldHaveId() {
        assertThat(serializedPredicate())
            .extractingJsonPathStringValue("@.id")
            .isEqualTo("P100")
    }

    @Test
    fun serializedPredicateShouldHaveLabel() {
        assertThat(serializedPredicate())
            .extractingJsonPathStringValue("@.label")
            .isEqualTo("label")
    }

    @Test
    fun serializedPredicateShouldHaveCreatedTimestamp() {
        assertThat(serializedPredicate())
            .extractingJsonPathStringValue("@.created_at")
            .isEqualTo("2018-12-25T05:23:42.123456789+03:00")
    }

    private fun createPredicate() =
        with(predicateRepresentationAdapter) {
            Predicate(
                ThingId("P100"),
                "label",
                OffsetDateTime.of(2018, 12, 25, 5, 23, 42, 123456789, ZoneOffset.ofHours(3))
            ).toPredicateRepresentation()
        }

    private fun serializedPredicate() = json.write(createPredicate())
}
