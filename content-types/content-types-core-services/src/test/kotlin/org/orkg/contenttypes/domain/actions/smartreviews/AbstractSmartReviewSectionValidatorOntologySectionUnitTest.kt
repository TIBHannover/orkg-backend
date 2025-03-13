package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.input.testing.fixtures.smartReviewOntologySectionCommand
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createPredicate
import java.util.Optional

internal class AbstractSmartReviewSectionValidatorOntologySectionUnitTest : AbstractSmartReviewSectionValidatorUnitTest() {
    @Test
    fun `Given an ontology section definition, when validating, it returns success`() {
        val section = smartReviewOntologySectionCommand().copy(predicates = listOf(Predicates.employs))
        val validIds = mutableSetOf<ThingId>()
        val predicate = createPredicate(section.predicates.single())

        every { thingRepository.existsAllById(section.entities.toSet()) } returns true
        every { predicateRepository.findById(predicate.id) } returns Optional.of(predicate)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe (section.entities union section.predicates)

        verify(exactly = 1) { thingRepository.existsAllById(section.entities.toSet()) }
        verify(exactly = 1) { predicateRepository.findById(predicate.id) }
    }

    @Test
    fun `Given an ontology section definition, when validating, it does query the thing repository when entity list is empty`() {
        val section = smartReviewOntologySectionCommand().copy(entities = emptyList())
        val validIds = mutableSetOf<ThingId>()
        val predicate = createPredicate(section.predicates.single())

        every { predicateRepository.findById(predicate.id) } returns Optional.of(predicate)

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(predicate.id)

        verify(exactly = 1) { predicateRepository.findById(predicate.id) }
    }

    @Test
    fun `Given an ontology section definition, when validating, it does query the predicate repository when predicate list is empty`() {
        val section = smartReviewOntologySectionCommand().copy(predicates = emptyList())
        val validIds = mutableSetOf<ThingId>()

        every { thingRepository.existsAllById(section.entities.toSet()) } returns true

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe (section.entities union section.predicates)

        verify(exactly = 1) { thingRepository.existsAllById(section.entities.toSet()) }
    }

    @Test
    fun `Given an ontology section definition, when validating, it does not check already valid ids`() {
        val section = smartReviewOntologySectionCommand()
        val validIds = mutableSetOf(ThingId("P1"))

        every { thingRepository.existsAllById(setOf(ThingId("R1"))) } returns true

        abstractSmartReviewSectionValidator.validate(section, validIds)

        validIds shouldBe (section.entities union section.predicates)

        verify(exactly = 1) { thingRepository.existsAllById(setOf(ThingId("R1"))) }
    }

    @Test
    fun `Given an ontology section definition, when heading is invalid, it throws an exception`() {
        val section = smartReviewOntologySectionCommand().copy(
            heading = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractSmartReviewSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given an ontology section definition, when entity does not exist, it throws an exception`() {
        val section = smartReviewOntologySectionCommand()
        val validIds = mutableSetOf<ThingId>()

        every { thingRepository.existsAllById(any()) } returns false

        assertThrows<OntologyEntityNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { thingRepository.existsAllById(any()) }
    }

    @Test
    fun `Given an ontology section definition, when predicate does not exist, it throws an exception`() {
        val section = smartReviewOntologySectionCommand().copy(predicates = listOf(Predicates.employs))
        val validIds = mutableSetOf<ThingId>()

        every { thingRepository.existsAllById(any()) } returns true
        every { predicateRepository.findById(section.predicates.single()) } returns Optional.empty()

        assertThrows<PredicateNotFound> { abstractSmartReviewSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { thingRepository.existsAllById(any()) }
        verify(exactly = 1) { predicateRepository.findById(section.predicates.single()) }
    }
}
