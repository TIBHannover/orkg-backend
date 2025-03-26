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
    fun `Given a text section command, when validating, it returns success`() {
        val section = smartReviewTextSectionCommand()
        val validationCache = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validationCache)

        validationCache shouldBe emptySet()
    }

    @Test
    fun `Given a text section command, when heading is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validationCache = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validationCache) }
    }

    @Test
    fun `Given a text section command, when text is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            text = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validationCache = mutableSetOf<ThingId>()

        assertThrows<InvalidDescription> { abstractSmartReviewSectionValidator.validate(section, validationCache) }
    }

    @Test
    fun `Given a text section command, when type is invalid, it throws an exception`() {
        val section = smartReviewTextSectionCommand().copy(
            `class` = Classes.comparison
        )
        val validationCache = mutableSetOf<ThingId>()

        assertThrows<InvalidSmartReviewTextSectionType> { abstractSmartReviewSectionValidator.validate(section, validationCache) }
    }
}
