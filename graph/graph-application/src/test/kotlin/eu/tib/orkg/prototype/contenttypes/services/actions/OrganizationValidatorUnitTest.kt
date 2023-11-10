package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.testing.fixtures.createOrganization
import eu.tib.orkg.prototype.community.testing.fixtures.toOrganizationEntity
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneOrganizationAllowed
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

class OrganizationValidatorUnitTest {
    private val organizationRepository: PostgresOrganizationRepository = mockk()

    private val organizationValidator = object : OrganizationValidator(organizationRepository) {}

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
        val organization = createOrganization(id).toOrganizationEntity()

        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)

        organizationValidator.validate(listOf(id))

        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
    }

    @Test
    fun `Given a list of organizations, when organization is missing, it throws an exception`() {
        val id = OrganizationId(UUID.randomUUID())

        every { organizationRepository.findById(id.value) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator.validate(listOf(id)) }

        verify(exactly = 1) { organizationRepository.findById(id.value) }
    }

    @Test
    fun `Given a list of organizations, when more than one organization is specified, it throws an exception`() {
        val ids = listOf(OrganizationId(UUID.randomUUID()), OrganizationId(UUID.randomUUID()))
        assertThrows<OnlyOneOrganizationAllowed> { organizationValidator.validate(ids) }
    }
}
