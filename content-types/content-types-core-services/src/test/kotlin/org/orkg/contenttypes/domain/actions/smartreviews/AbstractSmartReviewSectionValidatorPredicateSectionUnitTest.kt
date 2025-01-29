package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.testing.fixtures.smartReviewPredicateSectionDefinition
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.testing.fixtures.createPredicate

internal class AbstractSmartReviewSectionValidatorPredicateSectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given a predicate section definition, when validating, it returns success`() {
        val section = smartReviewPredicateSectionDefinition()
        val validIds = mutableSetOf<ThingId>()
        val predicate = createPredicate(section.predicate!!)

        every { predicateRepository.findById(section.predicate!!) } returns Optional.of(predicate)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.predicate)

        verify(exactly = 1) { predicateRepository.findById(section.predicate!!) }
    }

    @Test
    fun `Given a predicate section definition, when validating, it does not validate the predicate id when it is not set`() {
        val section = smartReviewPredicateSectionDefinition().copy(predicate = null)
        val validIds = mutableSetOf<ThingId>()

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe emptySet()
    }

    @Test
    fun `Given a predicate section definition, when validating, it does not check already valid ids`() {
        val section = smartReviewPredicateSectionDefinition()
        val validIds = mutableSetOf(section.predicate!!)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(section.predicate)
    }

    @Test
    fun `Given a predicate section definition, when heading is invalid, it throws an exception`() {
        val section = smartReviewPredicateSectionDefinition().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a predicate section definition, when predicate does not exist, it throws an exception`() {
        val section = smartReviewPredicateSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        every { predicateRepository.findById(section.predicate!!) } returns Optional.empty()

        assertThrows<PredicateNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { predicateRepository.findById(section.predicate!!) }
    }
}
