package org.orkg.contenttypes.domain.actions.contribution

import io.kotest.assertions.asClue
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
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.testing.fixtures.dummyCreateContributionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createResource

class ContributionPaperExistenceValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val contributionExistenceValidator = ContributionPaperExistenceValidator(
        resourceRepository = resourceRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, statementRepository, resourceService)
    }

    @Test
    fun `Given a contribution create command, when searching for existing papers, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()
        val paper = createResource(id = command.paperId, classes = setOf(Classes.paper))

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.of(paper)

        val result = contributionExistenceValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findPaperById(command.paperId) }
    }

    @Test
    fun `Given a contribution create command, when paper does not exist, it throws an exception`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.empty()

        assertThrows<PaperNotFound> { contributionExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findPaperById(command.paperId) }
    }
}
