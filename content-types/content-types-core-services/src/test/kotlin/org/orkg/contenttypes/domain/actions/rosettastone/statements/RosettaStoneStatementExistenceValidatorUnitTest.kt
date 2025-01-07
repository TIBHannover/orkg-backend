package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand

internal class RosettaStoneStatementExistenceValidatorUnitTest : MockkBaseTest {
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases = mockk()

    private val rosettaStoneStatementExistenceValidator = RosettaStoneStatementExistenceValidator(rosettaStoneStatementService)

    @Test
    fun `Given a rosetta stone statement update command, when checking for rosetta stone statement existence, it returns success`() {
        val rosettaStoneStatement = createRosettaStoneStatement()
        val command = dummyUpdateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState()

        every { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) } returns Optional.of(rosettaStoneStatement)

        rosettaStoneStatementExistenceValidator(command, state).asClue {
            it.rosettaStoneStatement shouldBe rosettaStoneStatement
        }

        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) }
    }

    @Test
    fun `Given a rosetta stone statement update command, when checking for rosetta stone statement existence and rosetta stone statement is not found, it throws an exception`() {
        val rosettaStoneStatement = createRosettaStoneStatement()
        val command = dummyUpdateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState()

        every { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) } returns Optional.empty()

        shouldThrow<RosettaStoneStatementNotFound> { rosettaStoneStatementExistenceValidator(command, state) }

        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) }
    }
}
