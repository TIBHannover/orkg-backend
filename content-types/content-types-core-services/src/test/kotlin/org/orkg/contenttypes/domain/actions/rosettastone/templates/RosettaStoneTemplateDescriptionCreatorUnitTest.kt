package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand
import org.orkg.graph.domain.Predicates

class RosettaStoneTemplateDescriptionCreatorUnitTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()

    private val rosettaStoneTemplateDescriptionCreator = RosettaStoneTemplateDescriptionCreator(singleStatementPropertyCreator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(singleStatementPropertyCreator)
    }

    @Test
    fun `Given a rosetta template create command, then it creates a description statement`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val rosettaStoneTemplateId = ThingId("R123")
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = rosettaStoneTemplateId
        )

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.rosettaStoneTemplateId!!,
                predicateId = Predicates.description,
                label = command.description
            )
        } just runs

        val result = rosettaStoneTemplateDescriptionCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.rosettaStoneTemplateId!!,
                predicateId = Predicates.description,
                label = command.description
            )
        }
    }
}
