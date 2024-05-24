package org.orkg.contenttypes.domain.actions.rosettastone.statements

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneStatementCommand

class RosettaStoneStatementExistenceValidatorUnitTest {
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases = mockk()

    private val rosettaStoneStatementExistenceValidator = RosettaStoneStatementExistenceValidator(rosettaStoneStatementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(rosettaStoneStatementService)
    }

    @Test
    fun `Given a rosetta stone statement update command, when checking for rosetta stone statement existence, it returns success`() {
        val rosettaStoneStatement = createDummyRosettaStoneStatement()
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
        val rosettaStoneStatement = createDummyRosettaStoneStatement()
        val command = dummyUpdateRosettaStoneStatementCommand().copy(id = rosettaStoneStatement.id)
        val state = UpdateRosettaStoneStatementState()

        every { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) } returns Optional.empty()

        shouldThrow<RosettaStoneStatementNotFound> { rosettaStoneStatementExistenceValidator(command, state) }

        verify(exactly = 1) { rosettaStoneStatementService.findByIdOrVersionId(rosettaStoneStatement.id) }
    }
}
