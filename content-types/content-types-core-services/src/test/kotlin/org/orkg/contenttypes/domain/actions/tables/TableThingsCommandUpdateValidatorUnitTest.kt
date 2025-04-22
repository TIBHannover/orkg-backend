package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TableThingsCommandUpdateValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val tableThingsCommandUpdateValidator = TableThingsCommandUpdateValidator(thingsCommandValidator)

    @Test
    fun `Given a table update command, when validating its thing commands, it returns success`() {
        val command = updateTableCommand()
        val state = UpdateTableState()

        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R100" to Either.right(createResource())
        )

        every { thingsCommandValidator.validate(command, state.validationCache) } returns validationCache

        val result = tableThingsCommandUpdateValidator(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) { thingsCommandValidator.validate(command, state.validationCache) }
    }

    @Test
    fun `Given a table update command, when rows are null, it does nothing`() {
        val command = updateTableCommand().copy(rows = null)
        val state = UpdateTableState()

        val result = tableThingsCommandUpdateValidator(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.statements shouldBe state.statements
            it.validationCache shouldBe state.validationCache
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }
    }
}
