package org.orkg.contenttypes.domain.actions.paper

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.domain.identifiers.InvalidIdentifier
import org.orkg.contenttypes.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

class PaperExistenceValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()

    private val paperExistenceValidator = PaperExistenceValidator(
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
}