package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
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
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionDeleter
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyDeleteSmartReviewSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewSectionDeleterUnitTest {
    private val abstractSmartReviewSectionDeleter: AbstractSmartReviewSectionDeleter = mockk()

    private val smartReviewSectionDeleter = SmartReviewSectionDeleter(abstractSmartReviewSectionDeleter)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractSmartReviewSectionDeleter)
    }

    @Test
    fun `Given a smart review section delete command, when section belongs to smart review, it deletes the section`() {
        val smartReview = createSmartReview()
        val command = dummyDeleteSmartReviewSectionCommand().copy(sectionId = smartReview.sections.last().id)
        val contributionId = ThingId("R456")
        val statements = listOf(
            createStatement(
                subject = createResource(command.smartReviewId),
                predicate = createPredicate(Predicates.hasContribution),
                `object` = createResource(contributionId, classes = setOf(Classes.contributionSmartReview))
            ),
            createStatement(
                subject = createResource(contributionId, classes = setOf(Classes.contributionSmartReview)),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(smartReview.sections.first().id)
            ),
            createStatement(
                subject = createResource(contributionId, classes = setOf(Classes.contributionSmartReview)),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(smartReview.sections.last().id)
            )
        )
        val state = DeleteSmartReviewSectionState().copy(
            smartReview = smartReview,
            statements = statements.groupBy { it.subject.id }
        )

        every {
            abstractSmartReviewSectionDeleter.delete(
                contributorId = command.contributorId,
                contributionId = contributionId,
                section = smartReview.sections.last(),
                statements = state.statements
            )
        } just runs

        val result = smartReviewSectionDeleter(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
        }

        verify(exactly = 1) {
            abstractSmartReviewSectionDeleter.delete(
                contributorId = command.contributorId,
                contributionId = contributionId,
                section = smartReview.sections.last(),
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a smart review section delete command, when section does not belong to smart review, it does nothing`() {
        val smartReview = createSmartReview()
        val command = dummyDeleteSmartReviewSectionCommand().copy(sectionId = ThingId("R123"))
        val contributionId = ThingId("R456")
        val statements = listOf(
            createStatement(
                subject = createResource(command.smartReviewId),
                predicate = createPredicate(Predicates.hasContribution),
                `object` = createResource(contributionId, classes = setOf(Classes.contributionSmartReview))
            ),
            createStatement(
                subject = createResource(contributionId, classes = setOf(Classes.contributionSmartReview)),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(smartReview.sections.first().id)
            ),
            createStatement(
                subject = createResource(contributionId, classes = setOf(Classes.contributionSmartReview)),
                predicate = createPredicate(Predicates.hasSection),
                `object` = createResource(smartReview.sections.last().id)
            )
        )
        val state = DeleteSmartReviewSectionState().copy(
            smartReview = smartReview,
            statements = statements.groupBy { it.subject.id }
        )

        val result = smartReviewSectionDeleter(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
        }
    }
}
