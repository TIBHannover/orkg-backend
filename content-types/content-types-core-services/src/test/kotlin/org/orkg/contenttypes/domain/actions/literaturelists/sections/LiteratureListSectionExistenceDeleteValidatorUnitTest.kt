package org.orkg.contenttypes.domain.actions.literaturelists.sections

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
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListExistenceValidator
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyDeleteLiteratureListSectionCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class LiteratureListSectionExistenceDeleteValidatorUnitTest {
    private val abstractLiteratureListExistenceValidator: AbstractLiteratureListExistenceValidator = mockk()

    private val literatureListSectionExistenceDeleteValidator =
        LiteratureListSectionExistenceDeleteValidator(abstractLiteratureListExistenceValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractLiteratureListExistenceValidator)
    }

    @Test
    fun `Given a literature list section delete command, when checking for literature list existence, it returns success`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyDeleteLiteratureListSectionCommand().copy(literatureListId = literatureList.id)
        val state = DeleteLiteratureListSectionState()
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every {
            abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(literatureList.id)
        } returns (literatureList to statements)

        literatureListSectionExistenceDeleteValidator(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.statements shouldBe statements
        }

        verify(exactly = 1) {
            abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(literatureList.id)
        }
    }
}
