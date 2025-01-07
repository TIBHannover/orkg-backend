package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

internal class ResearchFieldValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val researchFieldValidator = ResearchFieldValidator<List<ThingId>?, List<ThingId>>(resourceRepository, { it }, { it })

    @Test
    fun `Given a list of research fields, when validating, it returns success`() {
        val id = ThingId("R12")
        val researchField = createResource(id, classes = setOf(Classes.researchField))

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        researchFieldValidator(listOf(id), emptyList())

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a list of research fields, when research field is missing, it throws an exception`() {
        val id = ThingId("R12")

        every { resourceRepository.findById(id) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { researchFieldValidator(listOf(id), emptyList()) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }

    @Test
    fun `Given a list of research fields, when resource its not a research field, it throws an exception`() {
        val id = ThingId("R12")
        val researchField = createResource(id)

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        assertThrows<ResearchFieldNotFound> { researchFieldValidator(listOf(id), emptyList()) }

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a list of research fields, when more than one research field is specified, it throws an exception`() {
        val ids = listOf(ThingId("R12"), ThingId("R11"))
        assertThrows<OnlyOneResearchFieldAllowed> { researchFieldValidator(ids, emptyList()) }
    }

    @Test
    fun `Given a list of research fields, when old list of research fields is identical, it does nothing`() {
        val ids = listOf(ThingId("R12"), ThingId("R11"))
        researchFieldValidator(ids, ids)
    }

    @Test
    fun `Given a list of research fields, when no new research field is set, it does nothing`() {
        val ids = listOf(ThingId("R12"), ThingId("R11"))
        researchFieldValidator(null, ids)
    }
}
