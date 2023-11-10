package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreateContributionCommand
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
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
