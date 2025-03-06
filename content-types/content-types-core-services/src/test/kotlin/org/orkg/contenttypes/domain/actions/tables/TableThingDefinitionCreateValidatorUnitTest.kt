package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TableThingDefinitionCreateValidatorUnitTest : MockkBaseTest {
    private val thingDefinitionValidator: ThingDefinitionValidator = mockk()

    private val tableThingDefinitionCreateValidator = TableThingDefinitionCreateValidator(thingDefinitionValidator)

    @Test
    fun `Given a table create command, when validating its thing definitions, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState()

        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R100" to Either.right(createResource())
        )

        every {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        } returns validatedIds

        val result = tableThingDefinitionCreateValidator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe validatedIds
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }

        verify(exactly = 1) {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        }
    }
}
