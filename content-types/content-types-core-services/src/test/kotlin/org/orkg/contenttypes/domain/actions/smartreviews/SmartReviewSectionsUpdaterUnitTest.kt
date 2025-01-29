package org.orkg.contenttypes.domain.actions.smartreviews

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.smartReviewTextSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewCommand
import org.orkg.contenttypes.input.testing.fixtures.toSmartReviewSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewSectionsUpdaterUnitTest : MockkBaseTest {
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator = mockk()
    private val abstractSmartReviewSectionDeleter: AbstractSmartReviewSectionDeleter = mockk()
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()

    private val smartReviewSectionsUpdater = SmartReviewSectionsUpdater(
        abstractSmartReviewSectionCreator, abstractSmartReviewSectionDeleter, statementCollectionPropertyUpdater
    )

    @Test
    fun `Given a smart review update command, when sections are not set, it does nothing`() {
        val smartReview = createSmartReview()
        val command = updateSmartReviewCommand().copy(
            sections = null
        )
        val state = UpdateSmartReviewState(
            smartReview = smartReview
        )

        smartReviewSectionsUpdater(command, state)
    }

    @Test
    fun `Given a smart review update command, when sections are unchanged, it does nothing`() {
        val smartReview = createSmartReview()
        val command = updateSmartReviewCommand().copy(
            sections = smartReview.sections.map { it.toSmartReviewSectionDefinition() }
        )
        val state = UpdateSmartReviewState(
            smartReview = smartReview
        )

        smartReviewSectionsUpdater(command, state)
    }

    @Test
    fun `Given a smart review update command, when a section is removed, it deletes the old section`() {
        val smartReview = createSmartReview()
        val command = updateSmartReviewCommand().copy(
            sections = smartReview.sections.dropLast(1).map { it.toSmartReviewSectionDefinition() }
        )
        val contributionId = ThingId("R1144651")
        val contributionResource = createResource(contributionId, classes = setOf(Classes.contributionSmartReview))
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = listOf(
                createStatement(
                    subject = createResource(command.smartReviewId),
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contributionResource
                ),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs
        every {
            abstractSmartReviewSectionDeleter.delete(
                contributorId = command.contributorId,
                contributionId = contributionId,
                section = smartReview.sections.last(),
                statements = state.statements
            )
        } just runs

        smartReviewSectionsUpdater(command, state)

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[contributionId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = smartReview.sections.dropLast(1).map { it.id }
            )
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
    fun `Given a smart review update command, when a section is added, it creates a new section`() {
        val smartReview = createSmartReview()
        val newSection = smartReviewTextSectionDefinition().copy(text = "new section")
        val contributionId = ThingId("R1144651")
        val command = updateSmartReviewCommand().copy(
            sections = smartReview.sections.map { it.toSmartReviewSectionDefinition() } + newSection
        )
        val contributionResource = createResource(contributionId, classes = setOf(Classes.contributionSmartReview))
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = listOf(
                createStatement(
                    subject = createResource(command.smartReviewId),
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contributionResource
                ),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )
        val newSectionId = ThingId("irrelevant")

        every {
            abstractSmartReviewSectionCreator.create(command.contributorId, newSection)
        } returns newSectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs

        smartReviewSectionsUpdater(command, state)

        verify(exactly = 1) { abstractSmartReviewSectionCreator.create(command.contributorId, newSection) }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[contributionId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = smartReview.sections.map { it.id } + newSectionId
            )
        }
    }

    @Test
    fun `Given a smart review update command, when a section is replaced, it deletes the old section and creates a new one`() {
        val smartReview = createSmartReview()
        val newSection = smartReviewTextSectionDefinition().copy(text = "new section")
        val contributionId = ThingId("R1144651")
        val command = updateSmartReviewCommand().copy(
            sections = smartReview.sections.dropLast(1).map { it.toSmartReviewSectionDefinition() } + newSection
        )
        val contributionResource = createResource(contributionId, classes = setOf(Classes.contributionSmartReview))
        val state = UpdateSmartReviewState(
            smartReview = smartReview,
            statements = listOf(
                createStatement(
                    subject = createResource(command.smartReviewId),
                    predicate = createPredicate(Predicates.hasContribution),
                    `object` = contributionResource
                ),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasSection)),
                createStatement(subject = contributionResource, predicate = createPredicate(Predicates.hasLink))
            ).groupBy { it.subject.id }
        )
        val newSectionId = ThingId("irrelevant")

        every {
            abstractSmartReviewSectionCreator.create(command.contributorId, newSection)
        } returns newSectionId
        every {
            statementCollectionPropertyUpdater.update(
                statements = any(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = any<List<ThingId>>()
            )
        } just runs
        every {
            abstractSmartReviewSectionDeleter.delete(
                contributorId = command.contributorId,
                contributionId = contributionId,
                section = smartReview.sections.last(),
                statements = state.statements
            )
        } just runs

        smartReviewSectionsUpdater(command, state)

        verify(exactly = 1) { abstractSmartReviewSectionCreator.create(command.contributorId, newSection) }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[contributionId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = contributionId,
                predicateId = Predicates.hasSection,
                objects = smartReview.sections.dropLast(1).map { it.id } + newSectionId
            )
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
}
