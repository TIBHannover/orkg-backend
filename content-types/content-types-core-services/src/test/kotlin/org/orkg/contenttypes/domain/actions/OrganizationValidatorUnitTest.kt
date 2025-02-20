package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.OrganizationId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import java.util.Optional
import java.util.UUID

internal class OrganizationValidatorUnitTest : MockkBaseTest {
    private val organizationRepository: OrganizationRepository = mockk()

    private val organizationValidator = OrganizationValidator<List<OrganizationId>?, List<OrganizationId>>(organizationRepository, { it }, { it })

    @Test
    fun `Given a list of organizations, when validating, it returns success`() {
        val id = OrganizationId(UUID.randomUUID())
        val organization = createOrganization(id)

        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)

        organizationValidator(listOf(id), emptyList())

        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
    }

    @Test
    fun `Given a list of organizations, when organization is missing, it throws an exception`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationRepository.findById(id) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator(listOf(id), emptyList()) }

        verify(exactly = 1) { organizationRepository.findById(id) }
    }

    @Test
    fun `Given a list of organizations, when more than one organization is specified, it throws an exception`() {
        val ids = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        assertThrows<OnlyOneOrganizationAllowed> { organizationValidator(ids, emptyList()) }
    }

    @Test
    fun `Given a list of observatories, when old list of observatories is identical, it does nothing`() {
        val ids = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        organizationValidator(ids, ids)
    }

    @Test
    fun `Given a list of observatories, when no new observatories list is set, it does nothing`() {
        val ids = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        organizationValidator(null, ids)
    }
}
