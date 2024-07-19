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
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class SmartReviewReferencesUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val smartReviewReferencesUpdater = SmartReviewReferencesUpdater(statementCollectionPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater)
    }

    @Test
    fun `Given a smart review update command, when references are not set, it does nothing`() {
        val smartReview = createDummySmartReview()
        val command = dummyUpdateSmartReviewCommand().copy(references = null)
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewReferencesUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when references are unchanged, it does nothing`() {
        val smartReview = createDummySmartReview().copy(
            references = listOf(
                "@misc{R123456,title = {Fancy title of a super important paper}",
                "@misc{R456789,title = {Another super important paper}"
            )
        )
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = smartReview)

        val result = smartReviewReferencesUpdater(command, state)

        result.asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when references have changed, it updates the reference statements`() {
        val smartReview = createDummySmartReview()
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = listOf(
                createStatement(subject = createResource(command.smartReviewId), predicate = createPredicate(Predicates.hasReference)),
                createStatement(subject = createResource(command.smartReviewId), predicate = createPredicate(Predicates.hasContent)),
                createStatement(subject = createResource())
            ).groupBy { it.subject.id }
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasReference,
                literals = command.references!!
            )
        } just runs

        val result = smartReviewReferencesUpdater(command, state)

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
                predicateId = Predicates.hasReference,
                literals = command.references!!
            )
        }
    }
}
