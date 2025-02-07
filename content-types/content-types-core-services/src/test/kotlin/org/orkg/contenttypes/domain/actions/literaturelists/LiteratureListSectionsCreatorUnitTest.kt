package org.orkg.contenttypes.domain.actions.literaturelists

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.createLiteratureListCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

internal class LiteratureListSectionsCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()

    private val literatureListSectionsCreator = LiteratureListSectionsCreator(
        unsafeStatementUseCases, abstractLiteratureListSectionCreator
    )

    @Test
    fun `Given a literature list create command, when sections are empty, it does nothing`() {
        val literatureListId = ThingId("R123")
        val command = createLiteratureListCommand().copy(
            sections = emptyList()
        )
        val state = CreateLiteratureListState(
            literatureListId = literatureListId
        )

        literatureListSectionsCreator(command, state)
    }

    @Test
    fun `Given a literature list create command, when sections are not empty, it creates each section and links it to the literature list`() {
        val literatureListId = ThingId("R123")
        val command = createLiteratureListCommand()
        val state = CreateLiteratureListState(
            literatureListId = literatureListId
        )
        val sectionId1 = ThingId("R456")
        val sectionId2 = ThingId("R789")

        every { abstractLiteratureListSectionCreator.create(command.contributorId, any()) } returns sectionId1 andThen sectionId2
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        literatureListSectionsCreator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(command.contributorId, command.sections.first())
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.literatureListId!!,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId1
                )
            )
        }
        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(command.contributorId, command.sections.last())
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.literatureListId!!,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId2
                )
            )
        }
    }
}
