package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.PaperAlreadyExists
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreateContributionCommand
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.identifiers.application.InvalidIdentifier
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.statements.testing.fixtures.createStatement
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
import org.springframework.data.domain.Page

class PaperExistenceValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val paperExistenceValidator = PaperExistenceValidator(
        resourceRepository = resourceRepository,
        resourceService = resourceService,
        statementRepository = statementRepository
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
    fun `Given a paper create command, when searching for existing papers, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val doi = command.identifiers["doi"]!!

        every { resourceService.findAllByTitle(command.title) } returns Page.empty()
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        } returns Page.empty()

        val result = paperExistenceValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, and title matches, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val paper = createResource(label = command.title)
        val expected = PaperAlreadyExists.withTitle(paper.label)

        every { resourceService.findAllByTitle(command.title) } returns pageOf(paper)

        assertThrows<PaperAlreadyExists> { paperExistenceValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
    }

    @Test
    fun `Given a paper create command, when searching for existing papers, and identifier matches, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val doi = command.identifiers["doi"]!!
        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasDOI),
            `object` = createLiteral(label = doi)
        )
        val expected = PaperAlreadyExists.withIdentifier(doi)

        every { resourceService.findAllByTitle(command.title) } returns Page.empty()
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        } returns pageOf(statement)

        assertThrows<PaperAlreadyExists> { paperExistenceValidator(command, state) }.message shouldBe expected.message

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a paper create command, when paper identifier is structurally invalid, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            identifiers = mapOf(
                "doi" to "invalid"
            )
        )
        val state = PaperState()

        every { resourceService.findAllByTitle(command.title) } returns pageOf()

        assertThrows<InvalidIdentifier> { paperExistenceValidator(command, state) }.property shouldBe "doi"

        verify(exactly = 1) { resourceService.findAllByTitle(command.title) }
    }

    @Test
    fun `Given a contribution create command, when searching for existing papers, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()
        val paper = createResource(id = command.paperId, classes = setOf(Classes.paper))

        every { resourceRepository.findPaperById(command.paperId) } returns Optional.of(paper)

        val result = paperExistenceValidator(command, state)

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

        assertThrows<PaperNotFound> { paperExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findPaperById(command.paperId) }
    }
}
