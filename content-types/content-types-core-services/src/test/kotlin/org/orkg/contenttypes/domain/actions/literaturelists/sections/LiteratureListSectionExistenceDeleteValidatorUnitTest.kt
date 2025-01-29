package org.orkg.contenttypes.domain.actions.literaturelists.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.DeleteLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListExistenceValidator
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.deleteLiteratureListSectionCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class LiteratureListSectionExistenceDeleteValidatorUnitTest : MockkBaseTest {
    private val abstractLiteratureListExistenceValidator: AbstractLiteratureListExistenceValidator = mockk()

    private val literatureListSectionExistenceDeleteValidator =
        LiteratureListSectionExistenceDeleteValidator(abstractLiteratureListExistenceValidator)

    @Test
    fun `Given a literature list section delete command, when checking for literature list existence, it returns success`() {
        val literatureList = createLiteratureList()
        val command = deleteLiteratureListSectionCommand().copy(literatureListId = literatureList.id)
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
