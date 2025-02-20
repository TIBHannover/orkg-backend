package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.UnsafeStatementUseCases

internal class SmartReviewSectionsCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator = mockk()

    private val smartReviewSectionsCreator = SmartReviewSectionsCreator(
        unsafeStatementUseCases,
        abstractSmartReviewSectionCreator
    )

    @Test
    fun `Given a smart review create command, when sections are empty, it does nothing`() {
        val contributionId = ThingId("R123")
        val command = createSmartReviewCommand().copy(
            sections = emptyList()
        )
        val state = CreateSmartReviewState(contributionId = contributionId)

        smartReviewSectionsCreator(command, state)
    }

    @Test
    fun `Given a smart review create command, when sections are not empty, it creates each section and links it to the smart review`() {
        val contributionId = ThingId("R123")
        val command = createSmartReviewCommand()
        val state = CreateSmartReviewState(contributionId = contributionId)

        command.sections.forEachIndexed { index, section ->
            val sectionId = ThingId("Section$index")
            every { abstractSmartReviewSectionCreator.create(command.contributorId, section) } returns sectionId
            every {
                unsafeStatementUseCases.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        subjectId = contributionId,
                        predicateId = Predicates.hasSection,
                        objectId = sectionId
                    )
                )
            } returns StatementId("S1")
        }

        smartReviewSectionsCreator(command, state)

        command.sections.forEachIndexed { index, section ->
            verify(exactly = 1) {
                abstractSmartReviewSectionCreator.create(command.contributorId, section)
            }
            verify(exactly = 1) {
                unsafeStatementUseCases.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        subjectId = contributionId,
                        predicateId = Predicates.hasSection,
                        objectId = ThingId("Section$index")
                    )
                )
            }
        }
    }
}
