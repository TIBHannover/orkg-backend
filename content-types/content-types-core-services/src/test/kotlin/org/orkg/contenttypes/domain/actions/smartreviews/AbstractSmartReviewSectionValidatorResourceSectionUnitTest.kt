package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.testing.fixtures.smartReviewResourceSectionCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class AbstractSmartReviewSectionValidatorResourceSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a resource section command, when validating, it returns success`() {
        val section = smartReviewResourceSectionCommand()
        val validIds = mutableSetOf<ThingId>()
        val resource = createResource(section.resource!!)

        every { resourceRepository.findById(section.resource!!) } returns Optional.of(resource)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.resource)

        verify(exactly = 1) { resourceRepository.findById(section.resource!!) }
    }

    @Test
    fun `Given a resource section command, when validating, it does not validate the resource id when it is not set`() {
        val section = smartReviewResourceSectionCommand().copy(resource = null)
        val validIds = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe emptySet()
    }

    @Test
    fun `Given a resource section command, when validating, it does not check already valid ids`() {
        val section = smartReviewResourceSectionCommand()
        val validIds = mutableSetOf(section.resource!!)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.resource)
    }

    @Test
    fun `Given a resource section command, when heading is invalid, it throws an exception`() {
        val section = smartReviewResourceSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a resource section command, when resource does not exist, it throws an exception`() {
        val section = smartReviewResourceSectionCommand()
        val validIds = mutableSetOf<ThingId>()

        every { resourceRepository.findById(section.resource!!) } returns Optional.empty()

        assertThrows<ResourceNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(section.resource!!) }
    }
}
