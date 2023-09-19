package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.OnlyOneResearchFieldAllowed
import eu.tib.orkg.prototype.dummyCreatePaperCommand
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
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

class ResearchFieldValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val researchFieldValidator = ResearchFieldValidator(resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a paper create command, when validating its research field, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val researchField = createResource().copy(
            id = command.researchFields[0],
            classes = setOf(Classes.researchField)
        )

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        val result = researchFieldValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a paper create command, when research field is missing, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        every { resourceRepository.findById(command.researchFields[0]) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { researchFieldValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.researchFields[0]) }
    }

    @Test
    fun `Given a paper create command, when resource its not a research field, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val researchField = createResource().copy(
            id = command.researchFields[0]
        )

        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        assertThrows<ResearchFieldNotFound> { researchFieldValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given a paper create command, when more than one research field is specified, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            researchFields = listOf(ThingId("R12"), ThingId("R11"))
        )
        val state = PaperState()

        assertThrows<OnlyOneResearchFieldAllowed> { researchFieldValidator(command, state) }
    }
}
