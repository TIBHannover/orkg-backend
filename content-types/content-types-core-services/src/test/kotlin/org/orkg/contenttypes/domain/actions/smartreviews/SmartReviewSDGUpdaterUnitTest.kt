package org.orkg.contenttypes.domain.actions.smartreviews

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
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class SmartReviewSDGUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val smartReviewSDGUpdater = SmartReviewSDGUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a smart review update command, when SDGs are not set, it does nothing`() {
        val smartReview = createDummySmartReview()
        val command = dummyUpdateSmartReviewCommand().copy(sustainableDevelopmentGoals = null)
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewSDGUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when SDGs are unchanged, it does nothing`() {
        val smartReview = createDummySmartReview().copy(
            sustainableDevelopmentGoals = setOf(ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"))
        )
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewSDGUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when SDGs have changed, it updates the SDG statements`() {
        val smartReview = createDummySmartReview()
        val command = dummyUpdateSmartReviewCommand()
        val statements = listOf(
            createStatement(
                subject = createResource(command.smartReviewId),
                predicate = createPredicate(Predicates.sustainableDevelopmentGoal)
            ),
            createStatement(
                subject = createResource(command.smartReviewId),
                predicate = createPredicate(Predicates.hasContent)
            ),
            createStatement(subject = createResource())
        ).groupBy { it.subject.id }
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = statements
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!.toSet()
            )
        } just runs

        val result = smartReviewSDGUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements[command.smartReviewId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!.toSet()
            )
        }
    }
}
