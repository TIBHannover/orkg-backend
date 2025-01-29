package org.orkg.contenttypes.domain.actions.contributions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.input.testing.fixtures.createContributionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createResource

internal class ContributionPaperValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val contributionPaperValidator = ContributionPaperValidator(
        resourceRepository = resourceRepository
    )

    @Test
    fun `Given a contribution create command, when searching for existing papers, it returns success`() {
        val command = createContributionCommand()
        val state = ContributionState()
        val paper = createResource(id = command.paperId, classes = setOf(Classes.paper))

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.of(paper)

        val result = contributionPaperValidator(command, state)

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
        val command = createContributionCommand()
        val state = ContributionState()

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.empty()

        assertThrows<PaperNotFound> { contributionPaperValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findPaperById(command.paperId) }
    }

    @Test
    fun `Given a contribution create command, when paper is not modifiable, it throws an exception`() {
        val command = createContributionCommand()
        val state = ContributionState()
        val paper = createResource(id = command.paperId, classes = setOf(Classes.paper), modifiable = false)

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.of(paper)

        assertThrows<PaperNotModifiable> { contributionPaperValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findPaperById(command.paperId) }
    }
}
