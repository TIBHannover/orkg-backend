package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.input.LiteratureListListSectionDefinition.Entry
import org.orkg.contenttypes.input.testing.fixtures.literatureListListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.literatureListTextSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class AbstractLiteratureListSectionValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val abstractLiteratureListSectionValidator = AbstractLiteratureListSectionValidator(resourceRepository)

    @Test
    fun `Given a list section definition, when validating, it returns success`() {
        val section = literatureListListSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        every {
            resourceRepository.findById(ThingId("R2315"))
        } returns Optional.of(createResource(ThingId("R2315"), classes = setOf(Classes.paper)))
        every {
            resourceRepository.findById(ThingId("R3512"))
        } returns Optional.of(createResource(ThingId("R3512"), classes = setOf(Classes.software)))

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe section.entries.map { it.id }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R2315")) }
        verify(exactly = 1) { resourceRepository.findById(ThingId("R3512")) }
    }

    @Test
    fun `Given a list section definition, when validating, it does not validate an id twice`() {
        val section = literatureListListSectionDefinition().copy(
            entries = listOf(
                Entry(ThingId("R2315")),
                Entry(ThingId("R2315"))
            )
        )
        val validIds = mutableSetOf<ThingId>()

        every {
            resourceRepository.findById(ThingId("R2315"))
        } returns Optional.of(createResource(ThingId("R2315"), classes = setOf(Classes.paper)))

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe setOf(ThingId("R2315"))

        verify(exactly = 1) { resourceRepository.findById(ThingId("R2315")) }
    }

    @Test
    fun `Given a list section definition, when validating, it does not check already valid ids`() {
        val section = literatureListListSectionDefinition()
        val validIds = mutableSetOf(ThingId("R2315"))

        every {
            resourceRepository.findById(ThingId("R3512"))
        } returns Optional.of(createResource(ThingId("R3512"), classes = setOf(Classes.paper)))

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe section.entries.map { it.id }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R3512")) }
    }

    @Test
    fun `Given a list section definition, when description is invalid, it throws an exception`() {
        val section = literatureListListSectionDefinition().copy(
            entries = listOf(Entry(ThingId("R2315"), "a".repeat(MAX_LABEL_LENGTH + 1)))
        )
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidDescription> { abstractLiteratureListSectionValidator.validate(section, validIds) }
    }

    @Test
    fun `Given a list section definition, when resource is not allowed, it throws an exception`() {
        val section = literatureListListSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        every {
            resourceRepository.findById(ThingId("R2315"))
        } returns Optional.of(createResource(ThingId("R2315"), classes = setOf(Classes.visualization)))

        assertThrows<InvalidListSectionEntry> { abstractLiteratureListSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R2315")) }
    }

    @Test
    fun `Given a text section definition, when validating, it returns success`() {
        val section = literatureListTextSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe validIds
    }

    @Test
    fun `Given a text section definition, when heading is invalid, it throws an exception`() {
        val section = literatureListTextSectionDefinition().copy(heading = "\n")
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractLiteratureListSectionValidator.validate(section, validIds) }.asClue {
            it.property shouldBe "heading"
        }
    }

    @Test
    fun `Given a text section definition, when text is invalid, it throws an exception`() {
        val section = literatureListTextSectionDefinition().copy(text = "\n")
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidDescription> { abstractLiteratureListSectionValidator.validate(section, validIds) }.asClue {
            it.property shouldBe "text"
        }
    }

    @Test
    fun `Given a text section definition, when heading size is too low, it throws an exception`() {
        val section = literatureListTextSectionDefinition().copy(headingSize = 0)
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidHeadingSize> { abstractLiteratureListSectionValidator.validate(section, validIds) }
    }
}
