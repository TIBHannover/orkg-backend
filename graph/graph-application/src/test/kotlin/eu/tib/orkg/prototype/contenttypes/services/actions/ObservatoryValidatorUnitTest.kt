package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.community.testing.fixtures.createObservatory
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneObservatoryAllowed
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

    private val observatoryValidator = object : ObservatoryValidator(observatoryRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(observatoryRepository)
    }

    @Test
    fun `Given a list of observatories, when validating, it returns success`() {
        val id = ObservatoryId(UUID.randomUUID())
        val observatory = createObservatory(id = id)

        every { observatoryRepository.findById(observatory.id) } returns Optional.of(observatory)

        observatoryValidator.validate(listOf(id))

        verify(exactly = 1) { observatoryRepository.findById(observatory.id) }
    }

    @Test
    fun `Given a list of observatories, when observatory is missing, it throws an exception`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryRepository.findById(id) } returns Optional.empty()

        assertThrows<ObservatoryNotFound> { observatoryValidator.validate(listOf(id)) }

        verify(exactly = 1) { observatoryRepository.findById(id) }
    }

    @Test
    fun `Given a list of observatories, when more than one observatory is specified, it throws an exception`() {
        val ids = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        assertThrows<OnlyOneObservatoryAllowed> { observatoryValidator.validate(ids) }
    }
}
