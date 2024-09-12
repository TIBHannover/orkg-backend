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
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createConferenceSeries
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed

class OrganizationOrConferenceValidatorUnitTest {
    private val organizationRepository: OrganizationRepository = mockk()
    private val conferenceSeriesRepository: ConferenceSeriesRepository = mockk()

    private val organizationValidator = OrganizationOrConferenceValidator<List<OrganizationId>?, List<OrganizationId>>(
        organizationRepository, conferenceSeriesRepository, { it }, { it }
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(organizationRepository, conferenceSeriesRepository)
    }

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
        val oid = OrganizationId(UUID.randomUUID())
        val cid = ConferenceSeriesId(oid.value)

        every { organizationRepository.findById(oid) } returns Optional.empty()
        every { conferenceSeriesRepository.findById(cid) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator(listOf(oid), emptyList()) }

        verify(exactly = 1) { organizationRepository.findById(oid) }
        verify(exactly = 1) { conferenceSeriesRepository.findById(cid) }
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

    @Test
    fun `Given a list of conferences, when validating, it returns success`() {
        val oid = OrganizationId(UUID.randomUUID())
        val cid = ConferenceSeriesId(oid.value)
        val conference = createConferenceSeries(cid)

        every { organizationRepository.findById(oid) } returns Optional.empty()
        every { conferenceSeriesRepository.findById(cid) } returns Optional.of(conference)

        organizationValidator(listOf(oid), emptyList())

        verify(exactly = 1) { organizationRepository.findById(oid) }
        verify(exactly = 1) { conferenceSeriesRepository.findById(cid) }
    }

    @Test
    fun `Given a list of conferences, when conference is missing, it throws an exception`() {
        val oid = OrganizationId(UUID.randomUUID())
        val cid = ConferenceSeriesId(oid.value)

        every { organizationRepository.findById(oid) } returns Optional.empty()
        every { conferenceSeriesRepository.findById(cid) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { organizationValidator(listOf(oid), emptyList()) }

        verify(exactly = 1) { organizationRepository.findById(oid) }
        verify(exactly = 1) { conferenceSeriesRepository.findById(cid) }
    }
}
