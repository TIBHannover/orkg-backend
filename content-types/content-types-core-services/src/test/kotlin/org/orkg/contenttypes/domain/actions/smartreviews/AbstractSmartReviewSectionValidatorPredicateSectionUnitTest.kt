package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.testing.fixtures.smartReviewPredicateSectionCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.testing.fixtures.createPredicate
import java.util.Optional

internal class AbstractSmartReviewSectionValidatorPredicateSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a predicate section command, when validating, it returns success`() {
        val section = smartReviewPredicateSectionCommand()
        val validationCache = mutableSetOf<ThingId>()
        val predicate = createPredicate(section.predicate!!)

        every { predicateRepository.findById(section.predicate!!) } returns Optional.of(predicate)

        abstractSmartReviewSectionValidator.validate(section, validationCache)

        validationCache shouldBe setOf(section.predicate)

        verify(exactly = 1) { predicateRepository.findById(section.predicate!!) }
    }

    @Test
    fun `Given a predicate section command, when validating, it does not validate the predicate id when it is not set`() {
        val section = smartReviewPredicateSectionCommand().copy(predicate = null)
        val validationCache = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validationCache)

        validationCache shouldBe emptySet()
    }

    @Test
    fun `Given a predicate section command, when validating, it does not check already valid ids`() {
        val section = smartReviewPredicateSectionCommand()
        val validationCache = mutableSetOf(section.predicate!!)

        abstractSmartReviewSectionValidator.validate(section, validationCache)

        validationCache shouldBe setOf(section.predicate)
    }

    @Test
    fun `Given a predicate section command, when heading is invalid, it throws an exception`() {
        val section = smartReviewPredicateSectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validationCache = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validationCache) }
    }

    @Test
    fun `Given a predicate section command, when predicate does not exist, it throws an exception`() {
        val section = smartReviewPredicateSectionCommand()
        val validationCache = mutableSetOf<ThingId>()

        every { predicateRepository.findById(section.predicate!!) } returns Optional.empty()

        assertThrows<PredicateNotFound> { abstractSmartReviewSectionValidator.validate(section, validationCache) }

        verify(exactly = 1) { predicateRepository.findById(section.predicate!!) }
    }
}
