package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.createObservatory
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.statements.testing.createResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ObservatoryServiceTests {

    private val repository: ObservatoryRepository = mockk()
    private val organizationRepository: PostgresOrganizationRepository = mockk()
    private val resourceUseCases: ResourceUseCases = mockk()

    private val service = ObservatoryService(repository, organizationRepository, resourceUseCases)

    @Test
    fun `Creating an observatory`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val organizationId = observatory.organizationIds.single()

        every { repository.save(observatory) } returns Unit
        every { organizationRepository.findById(organizationId.value) } returns Optional.of(
            OrganizationEntity(organizationId.value)
        )

        service.create(
            id = observatory.id,
            name = observatory.name!!,
            description = observatory.description!!,
            organizationId = organizationId,
            researchField = ThingId(observatory.researchField?.id!!),
            displayId = observatory.displayId!!
        )

        verify(exactly = 1) { repository.save(observatory) }
    }

    @Test
    fun `Creating an observatory without existing organization`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val oId = OrganizationId(UUID.randomUUID())
        every { repository.save(any()) } returns Unit
        every { organizationRepository.findById(oId.value) } returns Optional.empty()

        shouldThrow<OrganizationNotFound> {
            service.create(
                id = null,
                name = "test",
                description = "test",
                organizationId = oId,
                researchField = ThingId("R1"),
                displayId = "test"
            )
        }

        verify(exactly = 0) { repository.save(observatory) }
    }

    @Test
    fun `Find an observatory by name after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findByName(observatory.name!!) } returns Optional.of(observatory)
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.findByName(observatory.name!!)

        verify(exactly = 1) { repository.findByName(observatory.name!!) }
    }

    @Test
    fun `Find an observatory by displayId after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findByDisplayId(observatory.displayId!!) } returns Optional.of(observatory)
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.findByDisplayId(observatory.displayId!!)

        verify(exactly = 1) { repository.findByDisplayId(observatory.displayId!!) }
    }

    @Test
    fun `Find an observatory by Id after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findById(observatory.id!!) } returns Optional.of(observatory)
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.findById(observatory.id!!)

        verify(exactly = 1) { repository.findById(observatory.id!!) }
    }

    @Test
    fun `Find an observatory by research field after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()
        val newResearchField = ResearchField(newResource.id.value, newResource.label)
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val researchFieldId = ThingId(newResearchField.id!!)
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)

        every { repository.findByResearchField(researchFieldId, pageRequest) } returns page
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.findObservatoriesByResearchField(researchFieldId, pageRequest)

        verify(exactly = 1) { repository.findByResearchField(researchFieldId, pageRequest) }
    }

    @Test
    fun `Find an observatory by organization Id after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)
        val organizationId = observatory.organizationIds.single()

        every { repository.findByOrganizationId(organizationId, pageRequest) } returns page
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.findObservatoriesByOrganizationId(organizationId, pageRequest)

        verify(exactly = 1) { repository.findByOrganizationId(organizationId, pageRequest) }
    }

    @Test
    fun `List all observatories`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)

        every { repository.findAll(pageRequest) } returns page
        every { resourceUseCases.findById(ThingId(observatory.researchField?.id!!)) } returns Optional.of(newResource)

        service.listObservatories(pageRequest)

        verify(exactly = 1) { repository.findAll(pageRequest) }
    }
}
