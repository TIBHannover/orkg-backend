package org.orkg.contenttypes.domain.actions.literaturelists

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateLiteratureListCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionsCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator = mockk()

    private val literatureListSectionsCreator = LiteratureListSectionsCreator(
        statementService, abstractLiteratureListSectionCreator
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, abstractLiteratureListSectionCreator)
    }

    @Test
    fun `Given a literature list create command, when sections are empty, it does nothing`() {
        val literatureListId = ThingId("R123")
        val command = dummyCreateLiteratureListCommand().copy(
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
        val command = dummyCreateLiteratureListCommand()
        val state = CreateLiteratureListState(
            literatureListId = literatureListId
        )
        val sectionId1 = ThingId("R456")
        val sectionId2 = ThingId("R789")

        every { abstractLiteratureListSectionCreator.create(command.contributorId, any()) } returns sectionId1 andThen sectionId2
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.literatureListId!!,
                predicate = Predicates.hasSection,
                `object` = any()
            )
        } just runs

        literatureListSectionsCreator(command, state)

        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(command.contributorId, command.sections.first())
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.literatureListId!!,
                predicate = Predicates.hasSection,
                `object` = sectionId1
            )
        }
        verify(exactly = 1) {
            abstractLiteratureListSectionCreator.create(command.contributorId, command.sections.last())
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.literatureListId!!,
                predicate = Predicates.hasSection,
                `object` = sectionId2
            )
        }
    }
}
