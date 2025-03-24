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
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TableColumnsCreateValidatorUnitTest : MockkBaseTest {
    private val abstractTableColumnsValidator: AbstractTableColumnsValidator = mockk()

    private val tableColumnsCreateValidator = TableColumnsCreateValidator(abstractTableColumnsValidator)

    @Test
    fun `Given a table create command, when validating its thing commands, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState(
            validationCache = mapOf(
                "R123" to Either.right(createResource(ThingId("R123")))
            )
        )

        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R123" to Either.right(createResource(ThingId("R123"))),
            "#temp1" from command
        )

        every {
            abstractTableColumnsValidator.validate(
                thingCommands = command.all(),
                rows = command.rows,
                validationCacheIn = state.validationCache
            )
        } returns validationCache

        val result = tableColumnsCreateValidator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.validationCache shouldBe validationCache
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) {
            abstractTableColumnsValidator.validate(
                thingCommands = command.all(),
                rows = command.rows,
                validationCacheIn = state.validationCache
            )
        }
    }
}
