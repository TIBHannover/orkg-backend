package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

internal class SmartReviewSectionsCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator = mockk()

    private val smartReviewSectionsCreator = SmartReviewSectionsCreator(
        statementService, abstractSmartReviewSectionCreator
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, abstractSmartReviewSectionCreator)
    }

    @Test
    fun `Given a smart review create command, when sections are empty, it does nothing`() {
        val contributionId = ThingId("R123")
        val command = dummyCreateSmartReviewCommand().copy(
            sections = emptyList()
        )
        val state = CreateSmartReviewState(contributionId = contributionId)

        smartReviewSectionsCreator(command, state)
    }

    @Test
    fun `Given a smart review create command, when sections are not empty, it creates each section and links it to the smart review`() {
        val contributionId = ThingId("R123")
        val command = dummyCreateSmartReviewCommand()
        val state = CreateSmartReviewState(contributionId = contributionId)

        command.sections.forEachIndexed { index, section ->
            val sectionId = ThingId("Section$index")
            every { abstractSmartReviewSectionCreator.create(command.contributorId, section) } returns sectionId
            every {
                statementService.add(
                    userId = command.contributorId,
                    subject = contributionId,
                    predicate = Predicates.hasSection,
                    `object` = sectionId
                )
            } just runs
        }

        smartReviewSectionsCreator(command, state)

        command.sections.forEachIndexed { index, section ->
            verify(exactly = 1) {
                abstractSmartReviewSectionCreator.create(command.contributorId, section)
            }
            verify(exactly = 1) {
                statementService.add(
                    userId = command.contributorId,
                    subject = contributionId,
                    predicate = Predicates.hasSection,
                    `object` = ThingId("Section$index")
                )
            }
        }
    }
}
