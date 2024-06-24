package org.orkg.contenttypes.domain.actions.literaturelists.sections

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
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTextSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class LiteratureListSectionIndexValidatorUnitTest {
    private val statementRepository: StatementRepository = mockk()

    private val literatureListSectionIndexValidator =
        LiteratureListSectionIndexValidator(statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementRepository)
    }

    @Test
    fun `Given a literature list section create command, when index is not specified, it does not load any statements`() {
        val command = dummyCreateTextSectionCommand()
        val state = CreateLiteratureListSectionState()

        val result = literatureListSectionIndexValidator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe null
            it.statements shouldBe emptyMap()
        }
    }

    @Test
    fun `Given a literature list section create command, when index is specified and valid, it fetches and saves all hasSection statements to the state`() {
        val command = dummyCreateTextSectionCommand().copy(index = 7)
        val state = CreateLiteratureListSectionState()
        val statements = listOf(createStatement())

        every {
            statementRepository.findAll(
                pageable = PageRequests.ALL,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection
            )
        } returns pageOf(statements)

        val result = literatureListSectionIndexValidator(command, state)

        result.asClue {
            it.literatureListSectionId shouldBe null
            it.statements shouldBe statements.groupBy { statement -> statement.subject.id }
        }

        verify(exactly = 1) {
            statementRepository.findAll(
                pageable = PageRequests.ALL,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection
            )
        }
    }
}
