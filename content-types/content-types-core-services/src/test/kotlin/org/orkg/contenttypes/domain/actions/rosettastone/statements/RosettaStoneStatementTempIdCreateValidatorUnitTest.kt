package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementState
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneStatementCommand

internal class RosettaStoneStatementTempIdCreateValidatorUnitTest {
    private val rosettaStoneStatementTempIdCreateValidator = RosettaStoneStatementTempIdCreateValidator()

    @Test
    fun `Given a rosetta stone statement create command, when validating its temp ids, it returns success`() {
        val command = createRosettaStoneStatementCommand()
        val state = CreateRosettaStoneStatementState()

        val result = rosettaStoneStatementTempIdCreateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5")
            it.validatedIds shouldBe state.validatedIds
        }
    }

    @Test
    fun `Given a rosetta stone statement create command, when it has no new thing commands, it returns success`() {
        val command = createRosettaStoneStatementCommand().copy(
            resources = emptyMap(),
            predicates = emptyMap(),
            literals = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = CreateRosettaStoneStatementState()

        val result = rosettaStoneStatementTempIdCreateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe state.rosettaStoneTemplate
            it.rosettaStoneStatementId shouldBe state.rosettaStoneStatementId
            it.tempIds shouldBe emptySet()
            it.validatedIds shouldBe state.validatedIds
        }
    }
}
