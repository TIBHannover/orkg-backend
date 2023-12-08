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
import org.orkg.common.OrganizationId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

class OrganizationValidatorUnitTest {
    private val organizationRepository: OrganizationRepository = mockk()

    private val organizationValidator = OrganizationValidator<List<OrganizationId>, Unit>(organizationRepository) { it }

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(organizationRepository)
    }

    @Test
    fun `Given a list of organizations, when validating, it returns success`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = createOrganization(id)

        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)

        organizationValidator(listOf(id), Unit)

        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
    }

    @Test
    fun `Given a list of organizations, when organization is missing, it throws an exception`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationRepository.findById(id) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator(listOf(id), Unit) }

        verify(exactly = 1) { organizationRepository.findById(id) }
    }

    @Test
    fun `Given a list of organizations, when more than one organization is specified, it throws an exception`() {
        val ids = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        assertThrows<OnlyOneOrganizationAllowed> { organizationValidator(ids, Unit) }
    }
}
