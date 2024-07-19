package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.domain.testing.fixtures.toGroupedStatements
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand

class SmartReviewSectionUpdaterUnitTest {
    private val abstractSmartReviewSectionUpdater: AbstractSmartReviewSectionUpdater = mockk()

    private val smartReviewSectionUpdateValidator = SmartReviewSectionUpdater(abstractSmartReviewSectionUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractSmartReviewSectionUpdater)
    }

    @Test
    fun `Given a comparison section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewComparisonSection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a comparison section update command, when contents have changed, it updates the comparison section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewComparisonSection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand>()
            .copy(heading = "updated heading")

        every { abstractSmartReviewSectionUpdater.updateComparisonSection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updateComparisonSection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a visualization section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewVisualizationSection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a visualization section update command, when contents have changed, it updates the visualization section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewVisualizationSection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand>()
            .copy(heading = "updated heading")

        every { abstractSmartReviewSectionUpdater.updateVisualizationSection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updateVisualizationSection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a resource section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewResourceSection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a resource section update command, when contents have changed, it updates the resource section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewResourceSection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand>()
            .copy(heading = "updated heading")

        every { abstractSmartReviewSectionUpdater.updateResourceSection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updateResourceSection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a predicate section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewPredicateSection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a predicate section update command, when contents have changed, it updates the predicate section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewPredicateSection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand>()
            .copy(heading = "updated heading")

        every { abstractSmartReviewSectionUpdater.updatePredicateSection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updatePredicateSection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a ontology section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewOntologySection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a ontology section update command, when contents have changed, it updates the ontology section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewOntologySection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand>()
            .copy(heading = "updated heading")

        every { abstractSmartReviewSectionUpdater.updateOntologySection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updateOntologySection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    @Test
    fun `Given a text section update command, when contents are equal, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = smartReview.sections.filterIsInstance<SmartReviewTextSection>().single()
            .toUpdateCommand(contributorId, smartReview.id)

        smartReviewSectionUpdateValidator(command, state)
    }

    @Test
    fun `Given a text section update command, when contents have changed, it updates the text section`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val smartReview = createDummySmartReview()
        val oldSection = smartReview.sections.filterIsInstance<SmartReviewTextSection>().single()
        val state = UpdateSmartReviewSectionState(
            smartReview = smartReview,
            statements = oldSection.toGroupedStatements()
        )
        val command = oldSection.toUpdateCommand(contributorId, smartReview.id)
            .shouldBeInstanceOf<UpdateTextSectionCommand>()
            .copy(text = "updated text")

        every { abstractSmartReviewSectionUpdater.updateTextSection(any(), any(), any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionUpdater.updateTextSection(
                contributorId = command.contributorId,
                newSection = command,
                oldSection = oldSection,
                statements = state.statements
            )
        }
    }

    private fun SmartReviewSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ): UpdateSmartReviewSectionCommand =
        when (this) {
            is SmartReviewComparisonSection -> toUpdateCommand(contributorId, smartReviewId)
            is SmartReviewVisualizationSection -> toUpdateCommand(contributorId, smartReviewId)
            is SmartReviewResourceSection -> toUpdateCommand(contributorId, smartReviewId)
            is SmartReviewPredicateSection -> toUpdateCommand(contributorId, smartReviewId)
            is SmartReviewOntologySection -> toUpdateCommand(contributorId, smartReviewId)
            is SmartReviewTextSection -> toUpdateCommand(contributorId, smartReviewId)
        }

    private fun SmartReviewComparisonSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        comparison = comparison?.id
    )

    private fun SmartReviewVisualizationSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        visualization = visualization?.id
    )

    private fun SmartReviewResourceSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        resource = resource?.id
    )

    private fun SmartReviewPredicateSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        predicate = predicate?.id
    )

    private fun SmartReviewOntologySection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        entities = entities.mapNotNull { it.id },
        predicates = predicates.map { it.id }
    )

    private fun SmartReviewTextSection.toUpdateCommand(
        contributorId: ContributorId,
        smartReviewId: ThingId
    ) = UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand(
        smartReviewSectionId = id,
        contributorId = contributorId,
        smartReviewId = smartReviewId,
        heading = heading,
        `class` = classes.filter { it in SmartReviewTextSection.types }.first(),
        text = text
    )
}
