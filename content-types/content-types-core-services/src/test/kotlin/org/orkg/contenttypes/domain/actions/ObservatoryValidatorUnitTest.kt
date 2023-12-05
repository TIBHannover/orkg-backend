package org.orkg.contenttypes.domain.actions

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
import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed

class ObservatoryValidatorUnitTest {
    private val observatoryRepository: ObservatoryRepository = mockk()

    private val observatoryValidator = ObservatoryValidator<List<ObservatoryId>, Unit>(observatoryRepository) { it }

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

        observatoryValidator(listOf(id), Unit)

        verify(exactly = 1) { observatoryRepository.findById(observatory.id) }
    }

    @Test
    fun `Given a list of observatories, when observatory is missing, it throws an exception`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryRepository.findById(id) } returns Optional.empty()

        assertThrows<ObservatoryNotFound> { observatoryValidator(listOf(id), Unit) }

        verify(exactly = 1) { observatoryRepository.findById(id) }
    }

    @Test
    fun `Given a list of observatories, when more than one observatory is specified, it throws an exception`() {
        val ids = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        assertThrows<OnlyOneObservatoryAllowed> { observatoryValidator(ids, Unit) }
    }
}
