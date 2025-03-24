package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.RowCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

private val header = RowCommand(
    label = "header",
    data = emptyList()
)

internal class AbstractTableCellsValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val abstractTableCellsValidator = AbstractTableCellsValidator(thingRepository)

    @Test
    fun `Given a list of table rows, when validating table cell ids, it returns success`() {
        val r123 = createResource(ThingId("R123"))
        val r456 = createResource(ThingId("R456"))
        val rows = listOf(
            header,
            RowCommand(
                label = "header",
                data = listOf("R123", "#temp2", null)
            ),
            RowCommand(
                label = "header",
                data = listOf("#temp1", "R456", "R456")
            )
        )
        val temp1 = CreateLiteralCommandPart("some label 1")
        val temp2 = CreateLiteralCommandPart("some label 2")
        val temp4 = CreateLiteralCommandPart("some label 4")
        val thingCommands = mapOf(
            "#temp1" to temp1,
            "#temp2" to temp2,
        )
        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R123" to Either.right(r123),
            "#temp4" to Either.left(temp4)
        )

        every { thingRepository.findById(ThingId("R456")) } returns Optional.of(r456)

        val result = abstractTableCellsValidator.validate(rows, thingCommands, validationCache)

        result shouldBe mapOf(
            "R123" to Either.right(r123),
            "R456" to Either.right(r456),
            "#temp1" to Either.left(temp1),
            "#temp2" to Either.left(temp2),
            "#temp4" to Either.left(temp4)
        )

        verify(exactly = 1) { thingRepository.findById(ThingId("R456")) }
    }

    @Test
    fun `Given a list of table rows, when validating table cell ids and temp id cannot be resolved, it throws an exception`() {
        val rows = listOf(
            header,
            RowCommand(
                label = "header",
                data = listOf("#temp1")
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        shouldThrow<ThingNotDefined> {
            abstractTableCellsValidator.validate(rows, emptyMap(), validationCache)
        }
    }

    @Test
    fun `Given a list of table rows, when validating table cell ids and thing id cannot be resolved, it throws an exception`() {
        val rows = listOf(
            header,
            RowCommand(
                label = "header",
                data = listOf("L123")
            )
        )
        val validationCache = emptyMap<String, Either<CreateThingCommandPart, Thing>>()

        every { thingRepository.findById(ThingId("L123")) } returns Optional.empty()

        shouldThrow<ThingNotFound> {
            abstractTableCellsValidator.validate(rows, emptyMap(), validationCache)
        }

        verify(exactly = 1) { thingRepository.findById(ThingId("L123")) }
    }
}
