package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.TemplatedResource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test the Formatted label class.
 */
class FormattedLabelTest {

    @Test
    fun formattedLabelCorrectlyComposed() {
        assertThat(createCorrectTemplatedResource())
            .isEqualTo("xxYYxx")
    }

    @Test
    fun `formatted label with an incorrect format remains unreplaced`() {
        assertThat(createTemplatedResourceWithWrongFormat())
            .isEqualTo("xx{P0xx")
    }

    @Test
    fun `formatted label with a missing property, the formatted label remains the same`() {
        assertThat(createTemplatedResourceWithMissingProperty())
            .isEqualTo("xx{P0}xx")
    }

    private fun createCorrectTemplatedResource() =
        createTemplatedResourceRepresentation(
            "xx{P0}xx",
            predicates = listOf("P0"),
            values = listOf("YY")
        )

    private fun createTemplatedResourceWithWrongFormat() =
        createTemplatedResourceRepresentation(
            "xx{P0xx",
            predicates = listOf("P0"),
            values = listOf("YY")
        )

    private fun createTemplatedResourceWithMissingProperty() =
        createTemplatedResourceRepresentation(
            "xx{P0}xx",
            predicates = listOf("P1"),
            values = listOf("YY")
        )

    private fun createTemplatedResourceRepresentation(
        format: String,
        predicates: List<String>,
        values: List<String>
    ): String =
        TemplatedResource(
            id = ThingId("R0"),
            label = "test template",
            classId = "C0",
            format = format,
            predicates = predicates,
            values = values
        ).composeFormattedLabel()
}
