package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneObservatoryAllowed
import eu.tib.orkg.prototype.dummyCreatePaperCommand
import eu.tib.orkg.prototype.createObservatory
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

class ObservatoryValidatorUnitTest {
    private val observatoryRepository: ObservatoryRepository = mockk()

    private val observatoryValidator = ObservatoryValidator(observatoryRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(observatoryRepository)
    }

    @Test
    fun `Given a paper create command, when validating its observatory, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()
        val observatory = createObservatory(id = command.observatories[0])

        every { observatoryRepository.findById(observatory.id) } returns Optional.of(observatory)

        val result = observatoryValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { observatoryRepository.findById(observatory.id) }
    }

    @Test
    fun `Given a paper create command, when observatory is missing, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        every { observatoryRepository.findById(command.observatories[0]) } returns Optional.empty()

        assertThrows<ObservatoryNotFound> { observatoryValidator(command, state) }

        verify(exactly = 1) { observatoryRepository.findById(command.observatories[0]) }
    }

    @Test
    fun `Given a paper create command, when more than one observatory is specified, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            observatories = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        )
        val state = PaperState()

        assertThrows<OnlyOneObservatoryAllowed> { observatoryValidator(command, state) }
    }
}
