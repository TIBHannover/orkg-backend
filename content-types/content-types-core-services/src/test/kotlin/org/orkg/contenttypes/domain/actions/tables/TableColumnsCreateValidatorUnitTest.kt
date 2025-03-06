package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TableColumnsCreateValidatorUnitTest : MockkBaseTest {
    private val abstractTableColumnsValidator: AbstractTableColumnsValidator = mockk()

    private val tableColumnsCreateValidator = TableColumnsCreateValidator(abstractTableColumnsValidator)

    @Test
    fun `Given a table create command, when validating its thing definitions, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "R123" to Either.right(createResource(ThingId("R123")))
            )
        )

        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R123" to Either.right(createResource(ThingId("R123"))),
            "t#emp1" to Either.left("#temp1")
        )

        every {
            abstractTableColumnsValidator.validate(
                thingDefinitions = command.all(),
                rows = command.rows,
                tempIds = state.tempIds,
                validationCacheIn = state.validatedIds
            )
        } returns validatedIds

        val result = tableColumnsCreateValidator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe validatedIds
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) {
            abstractTableColumnsValidator.validate(
                thingDefinitions = command.all(),
                rows = command.rows,
                tempIds = state.tempIds,
                validationCacheIn = state.validatedIds
            )
        }
    }
}
