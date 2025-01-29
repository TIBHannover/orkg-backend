package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneStatementCommand

internal class RosettaStoneStatementModifiableValidatorUnitTest {
    private val rosettaStoneStatementModifiableValidator = RosettaStoneStatementModifiableValidator()

    @Test
    fun `Given a rosetta stone statement update command, when rosetta stone statement is modifiable, it returns success`() {
        val rosettaStoneStatement = createRosettaStoneStatement().copy(modifiable = true)
        val command = updateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        assertDoesNotThrow { rosettaStoneStatementModifiableValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone statement update command, when rosetta stone statement is not modifiable, it throws an exception`() {
        val rosettaStoneStatement = createRosettaStoneStatement().copy(modifiable = false)
        val command = updateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState(rosettaStoneStatement = rosettaStoneStatement)

        shouldThrow<RosettaStoneStatementNotModifiable> { rosettaStoneStatementModifiableValidator(command, state) }
    }
}
