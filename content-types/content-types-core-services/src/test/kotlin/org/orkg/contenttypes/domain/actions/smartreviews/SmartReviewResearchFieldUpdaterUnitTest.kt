package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewResearchFieldUpdaterUnitTest : MockkBaseTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val smartReviewResearchFieldUpdater = SmartReviewResearchFieldUpdater(statementCollectionPropertyUpdater)

    @Test
    fun `Given a smart review update command, when research fields are not set, it does nothing`() {
        val smartReview = createSmartReview()
        val command = dummyUpdateSmartReviewCommand().copy(researchFields = null)
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewResearchFieldUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when research fields are unchanged, it does nothing`() {
        val smartReview = createSmartReview().copy(
            researchFields = listOf(ObjectIdAndLabel(ThingId("R12"), "Science"))
        )
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewResearchFieldUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when research fields have changed, it updates the research field statements`() {
        val smartReview = createSmartReview()
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = listOf(
                createStatement(subject = createResource(command.smartReviewId), predicate = createPredicate(Predicates.hasResearchField)),
                createStatement(subject = createResource(command.smartReviewId), predicate = createPredicate(Predicates.hasContent)),
                createStatement(subject = createResource())
            ).groupBy { it.subject.id }
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        } just runs

        val result = smartReviewResearchFieldUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.smartReviewId]!!,
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        }
    }
}
