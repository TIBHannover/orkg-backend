package org.orkg.contenttypes.domain.actions.smartreviews.sections

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
import org.junit.jupiter.api.assertThrows
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
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewVisualizationSectionCommand

class SmartReviewSectionUpdateValidatorUnitTest {
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator = mockk()

    private val smartReviewSectionUpdateValidator = SmartReviewSectionUpdateValidator(abstractSmartReviewSectionValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractSmartReviewSectionValidator)
    }

    @Test
    fun `Given a smart review section update command, when section is not related to the smart review, it throws an exception`() {
        val command = dummyUpdateSmartReviewComparisonSectionCommand()
        val state = UpdateSmartReviewSectionState(smartReview = createDummySmartReview())

        assertThrows<UnrelatedSmartReviewSection> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a comparison section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewComparisonSection>().first()
        val command = dummyUpdateSmartReviewComparisonSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.comparison!!.id))
        }
    }

    @Test
    fun `Given a comparison section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewComparisonSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a visualization section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewVisualizationSection>().first()
        val command = dummyUpdateSmartReviewVisualizationSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.visualization!!.id))
        }
    }

    @Test
    fun `Given a visualization section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewVisualizationSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a resource section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewResourceSection>().first()
        val command = dummyUpdateSmartReviewResourceSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.resource!!.id))
        }
    }

    @Test
    fun `Given a resource section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewResourceSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a predicate section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewPredicateSection>().first()
        val command = dummyUpdateSmartReviewPredicateSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf(section.predicate!!.id))
        }
    }

    @Test
    fun `Given a predicate section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewPredicateSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a ontology section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewOntologySection>().first()
        val command = dummyUpdateSmartReviewOntologySectionCommand().copy(smartReviewSectionId = section.id)
        val validIds = (section.entities.mapNotNull { it.id } union section.predicates.map { it.id }).toMutableSet()

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, validIds)
        }
    }

    @Test
    fun `Given a ontology section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewOntologySectionCommand().copy(smartReviewSectionId = smartReview.sections.first().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }

    @Test
    fun `Given a text section update command, when validation succeeds, it returns success`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val section = smartReview.sections.filterIsInstance<SmartReviewTextSection>().first()
        val command = dummyUpdateSmartReviewTextSectionCommand().copy(smartReviewSectionId = section.id)

        every { abstractSmartReviewSectionValidator.validate(any(), any()) } just runs

        smartReviewSectionUpdateValidator(command, state)

        verify(exactly = 1) {
            abstractSmartReviewSectionValidator.validate(command, mutableSetOf())
        }
    }

    @Test
    fun `Given a text section update command, when types mismatch, it throws an exception`() {
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewSectionState(smartReview = smartReview)
        val command = dummyUpdateSmartReviewTextSectionCommand().copy(smartReviewSectionId = smartReview.sections.last().id)

        assertThrows<SmartReviewSectionTypeMismatch> { smartReviewSectionUpdateValidator(command, state) }
    }
}
