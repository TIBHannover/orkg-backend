package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

internal class PaperIdentifierCreateValidatorUnitTest {
    private val statementRepository: StatementRepository = mockk()

    private val paperIdentifierCreateValidator = PaperIdentifierCreateValidator(statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementRepository)
    }

    @Test
    fun `Given a paper create command, when validating its identifiers, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()
        val doi = command.identifiers["doi"]!!.first()

        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        } returns Page.empty()

        paperIdentifierCreateValidator(command, state)

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a paper create command, when paper with identifier already exists, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState()
        val doi = command.identifiers["doi"]!!.first()

        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasDOI),
            `object` = createLiteral(label = doi)
        )
        val expected = PaperAlreadyExists.withIdentifier(doi)

        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        } returns pageOf(statement)

        val result = assertThrows<PaperAlreadyExists> {
            paperIdentifierCreateValidator(command, state)
        }
        result.message shouldBe expected.message

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        }
    }
}
