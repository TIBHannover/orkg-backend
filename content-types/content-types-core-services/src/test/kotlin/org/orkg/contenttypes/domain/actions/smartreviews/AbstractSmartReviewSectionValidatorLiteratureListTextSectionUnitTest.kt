package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.input.testing.fixtures.smartReviewTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH

internal class AbstractSmartReviewSectionValidatorLiteratureListTextSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a text section definition, when validating, it returns success`() {
        val section = smartReviewTextSectionCommand()
        val validIds = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe emptySet()
    }

    @Test
    fun `Given a text section definition, when heading is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a text section definition, when text is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            text = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidDescription> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a text section definition, when type is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            `class` = Classes.comparison
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidSmartReviewTextSectionType> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }
}
