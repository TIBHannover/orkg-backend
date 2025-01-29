package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionValidator
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewVisualizationSectionCommand

internal class SmartReviewSectionUpdateValidatorUnitTest : MockkBaseTest {
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator = mockk()

    private val smartReviewSectionUpdateValidator = SmartReviewSectionUpdateValidator(abstractSmartReviewSectionValidator)

    @Test
    fun `Given a smart review section update command, when section is not related to the smart review, it throws an exception`() {
        val command = updateSmartReviewComparisonSectionCommand()
        val state = UpdateSmartReviewSectionState(smartReview = createSmartReview())

        assertThrows<UnrelatedSmartReviewSection> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a comparison section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewComparisonSection>().first()
        val command = updateSmartReviewComparisonSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.comparison!!.id))
        }
    }

    @Test
    fun `Given a comparison section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewComparisonSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a visualization section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewVisualizationSection>().first()
        val command = updateSmartReviewVisualizationSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.visualization!!.id))
        }
    }

    @Test
    fun `Given a visualization section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewVisualizationSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a resource section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewResourceSection>().first()
        val command = updateSmartReviewResourceSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.resource!!.id))
        }
    }

    @Test
    fun `Given a resource section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewResourceSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a predicate section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewPredicateSection>().first()
        val command = updateSmartReviewPredicateSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.predicate!!.id))
        }
    }

    @Test
    fun `Given a predicate section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewPredicateSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a ontology section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewOntologySection>().first()
        val command = updateSmartReviewOntologySectionCommand().copy(smartReviewSectionId = section.id)
        val validIds = (section.entities.mapNotNull { it.id } union section.predicates.map { it.id }).toMutableSet()

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, validIds)
        }
    }

    @Test
    fun `Given a ontology section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewOntologySectionCommand().copy(smartReviewSectionId = smartReview.sections.first().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a text section update command, when validation succeeds, it returns success`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewTextSection>().first()
        val command = updateSmartReviewTextSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf())
        }
    }

    @Test
    fun `Given a text section update command, when types mismatch, it throws an exception`() {
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = updateSmartReviewTextSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }
}
