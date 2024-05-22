package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.input.testing.fixtures.dummyListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyTextSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

class AbstractLiteratureListSectionValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val abstractLiteratureListSectionValidator = AbstractLiteratureListSectionValidator(resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a list section definition, when validating, it returns success`() {
        val section = dummyListSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        every {
            resourceRepository.findById(ThingId("R2315"))
        } returns Optional.of(createResource(ThingId("R2315"), classes = setOf(Classes.paper)))
        every {
            resourceRepository.findById(ThingId("R3512"))
        } returns Optional.of(createResource(ThingId("R3512"), classes = setOf(Classes.software)))

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe section.entries

        verify(exactly = 1) { resourceRepository.findById(ThingId("R2315")) }
        verify(exactly = 1) { resourceRepository.findById(ThingId("R3512")) }
    }

    @Test
    fun `Given a list section definition, when validating, it does not validate an id twice`() {
        val section = dummyListSectionDefinition().copy(
            entries = listOf(ThingId("R2315"), ThingId("R2315"))
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
        val section = dummyListSectionDefinition()
        val validIds = mutableSetOf(ThingId("R2315"))

        every {
            resourceRepository.findById(ThingId("R3512"))
        } returns Optional.of(createResource(ThingId("R3512"), classes = setOf(Classes.paper)))

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe section.entries

        verify(exactly = 1) { resourceRepository.findById(ThingId("R3512")) }
    }

    @Test
    fun `Given a list section definition, when resource is not allowed, it throws an exception`() {
        val section = dummyListSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        every {
            resourceRepository.findById(ThingId("R2315"))
        } returns Optional.of(createResource(ThingId("R2315"), classes = setOf(Classes.visualization)))

        assertThrows<InvalidListSectionEntry> { abstractLiteratureListSectionValidator.validate(section, validIds) }

        verify(exactly = 1) { resourceRepository.findById(ThingId("R2315")) }
    }

    @Test
    fun `Given a text section definition, when validating, it returns success`() {
        val section = dummyTextSectionDefinition()
        val validIds = mutableSetOf<ThingId>()

        abstractLiteratureListSectionValidator.validate(section, validIds)

        validIds shouldBe validIds
    }

    @Test
    fun `Given a text section definition, when heading is invalid, it throws an exception`() {
        val section = dummyTextSectionDefinition().copy(heading = "\n")
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidLabel> { abstractLiteratureListSectionValidator.validate(section, validIds) }.asClue {
            it.property shouldBe "heading"
        }
    }

    @Test
    fun `Given a text section definition, when text is invalid, it throws an exception`() {
        val section = dummyTextSectionDefinition().copy(text = "\n")
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidDescription> { abstractLiteratureListSectionValidator.validate(section, validIds) }.asClue {
            it.property shouldBe "text"
        }
    }

    @Test
    fun `Given a text section definition, when heading size is too low, it throws an exception`() {
        val section = dummyTextSectionDefinition().copy(headingSize = 0)
        val validIds = mutableSetOf<ThingId>()

        assertThrows<InvalidHeadingSize> { abstractLiteratureListSectionValidator.validate(section, validIds) }
    }
}
