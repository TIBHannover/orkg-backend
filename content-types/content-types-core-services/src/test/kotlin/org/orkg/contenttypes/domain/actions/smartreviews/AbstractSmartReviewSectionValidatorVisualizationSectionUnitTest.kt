package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.testing.fixtures.smartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class AbstractSmartReviewSectionValidatorVisualizationSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a visualization section command, when validating, it returns success`() {
        val section = smartReviewVisualizationSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.visualization!!, classes = setOf(Classes.visualization))

        every { resourceRepository.findById(section.visualization!!) } returns Optional.of(resource)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.visualization)

        verify(exactly = 1) { resourceRepository.findById(section.visualization!!) }
    }

    @Test
    fun `Given a visualization section command, when validating, it does not validate the visualization id when it is not set`() {
        val section = smartReviewVisualizationSectionCommand().copy(visualization = null)
        val validIds = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe emptySet()
    }

    @Test
    fun `Given a visualization section command, when validating, it does not check already valid ids`() {
        val section = smartReviewVisualizationSectionCommand()
        val validIds = mutableSetOf(section.visualization!!)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.visualization)
    }

    @Test
    fun `Given a visualization section command, when heading is invalid, it throws an exception`() {
        val section = smartReviewVisualizationSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a visualization section command, when resource is not a visualization, it throws an exception`() {
        val section = smartReviewVisualizationSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.visualization!!, classes = setOf(Classes.comparison))

        every { resourceRepository.findById(section.visualization!!) } returns Optional.of(resource)

        assertThrows<VisualizationNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(section.visualization!!) }
    }

    @Test
    fun `Given a visualization section command, when visualization does not exist, it throws an exception`() {
        val section = smartReviewVisualizationSectionCommand()
        val validIds = mutableSetOf<ThingId>()

        every { resourceRepository.findById(section.visualization!!) } returns Optional.empty()

        assertThrows<VisualizationNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(section.visualization!!) }
    }
}
