package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.OnlyOneResearchFieldAllowed
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
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

class ResearchFieldValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val researchFieldValidator = object : ResearchFieldValidator(resourceRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a list of research fields, when validating, it returns success`() {
        val id = ThingId("R12")
        val researchField = createResource(id, classes = setOf(Classes.researchField))

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        researchFieldValidator.validate(listOf(id))

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a list of research fields, when research field is missing, it throws an exception`() {
        val id = ThingId("R12")

        every { resourceRepository.findById(id) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { researchFieldValidator.validate(listOf(id)) }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }

    @Test
    fun `Given a list of research fields, when resource its not a research field, it throws an exception`() {
        val id = ThingId("R12")
        val researchField = createResource(id)

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        assertThrows<ResearchFieldNotFound> { researchFieldValidator.validate(listOf(id)) }

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a list of research fields, when more than one research field is specified, it throws an exception`() {
        val ids = listOf(ThingId("R12"), ThingId("R11"))
        assertThrows<OnlyOneResearchFieldAllowed> { researchFieldValidator.validate(ids) }
    }
}
