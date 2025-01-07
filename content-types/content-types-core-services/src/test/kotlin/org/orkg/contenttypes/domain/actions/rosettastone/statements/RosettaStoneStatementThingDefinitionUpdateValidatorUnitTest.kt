package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class RosettaStoneStatementThingDefinitionUpdateValidatorUnitTest : MockkBaseTest {
    private val thingDefinitionValidator: ThingDefinitionValidator = mockk()

    private val stoneStatementThingDefinitionUpdateValidator = RosettaStoneStatementThingDefinitionUpdateValidator(thingDefinitionValidator)

    @Test
    fun `Given a rosetta stone statement update command, when validating its thing definitions, it returns success`() {
        val command = dummyUpdateRosettaStoneStatementCommand()
        val state = UpdateRosettaStoneStatementState()

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

        val result = stoneStatementThingDefinitionUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe validatedIds
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
