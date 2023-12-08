package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

class PaperIdentifierUpdateValidatorUnitTest {
    private val statementRepository: StatementRepository = mockk()

    private val paperIdentifierUpdateValidator = PaperIdentifierUpdateValidator(statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementRepository)
    }

    @Test
    fun `Given a paper update command, when validating its identifiers, it returns success`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()
        val doi = command.identifiers!!["doi"]!!

        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        } returns Page.empty()

        paperIdentifierUpdateValidator(command, state)

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
    fun `Given a paper update command, when paper with identifier already exists, it throws an exception`() {
        val command = dummyUpdatePaperCommand()
        val state = UpdatePaperState()
        val doi = command.identifiers!!["doi"]!!

        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasDOI),
            `object` = createLiteral(label = doi)
        )
        val expected = PaperAlreadyExists.withIdentifier(doi)

        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        } returns pageOf(statement)

        val result = assertThrows<PaperAlreadyExists> {
            paperIdentifierUpdateValidator(command, state)
        }
        result.message shouldBe expected.message

        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasDOI,
                literal = doi,
                subjectClass = Classes.paper,
                pageable = any()
            )
        }
    }
}
