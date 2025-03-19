package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.input.testing.fixtures.smartReviewComparisonSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class AbstractSmartReviewSectionValidatorComparisonSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a comparison section command, when validating, it returns success`() {
        val section = smartReviewComparisonSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.comparison!!, classes = setOf(Classes.comparison))

        every { resourceRepository.findById(section.comparison!!) } returns Optional.of(resource)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.comparison)

        verify(exactly = 1) { resourceRepository.findById(section.comparison!!) }
    }

    @Test
    fun `Given a comparison section command for a published comparison, when validating, it returns success`() {
        val section = smartReviewComparisonSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.comparison!!, classes = setOf(Classes.comparisonPublished))

        every { resourceRepository.findById(section.comparison!!) } returns Optional.of(resource)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.comparison)

        verify(exactly = 1) { resourceRepository.findById(section.comparison!!) }
    }

    @Test
    fun `Given a comparison section command, when validating, it does not validate the comparison id when it is not set`() {
        val section = smartReviewComparisonSectionCommand().copy(comparison = null)
        val validIds = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe emptySet()
    }

    @Test
    fun `Given a comparison section command, when validating, it does not check already valid ids`() {
        val section = smartReviewComparisonSectionCommand()
        val validIds = mutableSetOf(section.comparison!!)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.comparison)
    }

    @Test
    fun `Given a comparison section command, when heading is invalid, it throws an exception`() {
        val section = smartReviewComparisonSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a comparison section command, when resource is not a comparison, it throws an exception`() {
        val section = smartReviewComparisonSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.comparison!!, classes = setOf(Classes.visualization))

        every { resourceRepository.findById(section.comparison!!) } returns Optional.of(resource)

        assertThrows<ComparisonNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(section.comparison!!) }
    }

    @Test
    fun `Given a comparison section command, when comparison does not exist, it throws an exception`() {
        val section = smartReviewComparisonSectionCommand()
        val validIds = mutableSetOf<ThingId>()

        every { resourceRepository.findById(section.comparison!!) } returns Optional.empty()

        assertThrows<ComparisonNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(section.comparison!!) }
    }
}
