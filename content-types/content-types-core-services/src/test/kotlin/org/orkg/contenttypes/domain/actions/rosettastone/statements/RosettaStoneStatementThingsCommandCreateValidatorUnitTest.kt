package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneStatementCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class RosettaStoneStatementThingsCommandCreateValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val rosettaStoneStatementThingsCommandCreateValidator = RosettaStoneStatementThingsCommandCreateValidator(thingsCommandValidator)

    @Test
    fun `Given a rosetta stone statement create command, when validating its thing commands, it returns success`() {
        val command = createRosettaStoneStatementCommand()
        val state = CreateRosettaStoneStatementState()

        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R100" to Either.right(createResource())
        )

        every {
            thingsCommandValidator.validate(
                thingsCommand = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        } returns validatedIds

        val result = rosettaStoneStatementThingsCommandCreateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe validatedIds
        }

        verify(exactly = 1) {
            thingsCommandValidator.validate(
                thingsCommand = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        }
    }
}
