package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ObservatoryId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import java.util.Optional
import java.util.UUID

internal class ObservatoryValidatorUnitTest : MockkBaseTest {
    private val observatoryRepository: ObservatoryRepository = mockk()

    private val observatoryValidator = ObservatoryValidator<List<ObservatoryId>?, List<ObservatoryId>>(observatoryRepository, { it }, { it })

    @Test
    fun `Given a list of observatories, when validating, it returns success`() {
        val id = ObservatoryId(UUID.randomUUID())
        val observatory = createObservatory(id = id)

        every { observatoryRepository.findById(observatory.id) } returns Optional.of(observatory)

        observatoryValidator(listOf(id), emptyList())

        verify(exactly = 1) { observatoryRepository.findById(observatory.id) }
    }

    @Test
    fun `Given a list of observatories, when observatory is missing, it throws an exception`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryRepository.findById(id) } returns Optional.empty()

        assertThrows<ObservatoryNotFound> { observatoryValidator(listOf(id), emptyList()) }

        verify(exactly = 1) { observatoryRepository.findById(id) }
    }

    @Test
    fun `Given a list of observatories, when more than one observatory is specified, it throws an exception`() {
        val ids = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        assertThrows<OnlyOneObservatoryAllowed> { observatoryValidator(ids, emptyList()) }
    }

    @Test
    fun `Given a list of observatories, when old list of observatories is identical, it does nothing`() {
        val ids = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        observatoryValidator(ids, ids)
    }

    @Test
    fun `Given a list of observatories, when no new observatories list is set, it does nothing`() {
        val ids = listOf(ObservatoryId(UUID.randomUUID()), ObservatoryId(UUID.randomUUID()))
        observatoryValidator(null, ids)
    }
}
