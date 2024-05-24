package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand

class RosettaStoneStatementTempIdUpdateValidatorUnitTest {
    private val rosettaStoneStatementTempIdUpdateValidator = RosettaStoneStatementTempIdUpdateValidator()

    @Test
    fun `Given a rosetta stone statement update command, when validating its temp ids, it returns success`() {
        val command = dummyUpdateRosettaStoneStatementCommand()
        val state = UpdateRosettaStoneStatementState()

        val result = rosettaStoneStatementTempIdUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5")
            it.validatedIds shouldBe state.validatedIds
        }
    }

    @Test
    fun `Given a rosetta stone statement update command, when it has no new thing definitions, it returns success`() {
        val command = dummyUpdateRosettaStoneStatementCommand().copy(
            resources = emptyMap(),
            predicates = emptyMap(),
            literals = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateRosettaStoneStatementState()

        val result = rosettaStoneStatementTempIdUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe emptySet()
            it.validatedIds shouldBe state.validatedIds
        }
    }
}
