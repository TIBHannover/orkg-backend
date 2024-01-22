package org.orkg.community.domain

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ObservatoryServiceTests {

    private val repository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val service = ObservatoryService(repository, organizationRepository, resourceRepository)

    @Test
    fun `Creating an observatory`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val organizationId = observatory.organizationIds.single()
        val researchField = createResource(
            id = observatory.researchField!!,
            classes = setOf(ThingId("ResearchField"))
        )

        every { repository.save(observatory) } returns Unit
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(researchField)
        every { organizationRepository.findById(organizationId) } returns Optional.of(
            createOrganization(id = organizationId)
        )

        service.create(
            CreateObservatoryUseCase.CreateCommand(
                id = observatory.id,
                name = observatory.name,
                description = observatory.description!!,
                organizationId = organizationId,
                researchField = observatory.researchField!!,
                displayId = observatory.displayId
            )
        )

        verify(exactly = 1) { repository.save(observatory) }
    }

    @Test
    fun `Creating an observatory without existing organization`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val oId = OrganizationId(UUID.randomUUID())
        every { repository.save(any()) } returns Unit
        every { organizationRepository.findById(oId) } returns Optional.empty()

        shouldThrow<OrganizationNotFound> {
            service.create(
                CreateObservatoryUseCase.CreateCommand(
                    id = null,
                    name = "test",
                    description = "test",
                    organizationId = oId,
                    researchField = ThingId("R1"),
                    displayId = "test"
                )
            )
        }

        verify(exactly = 0) { repository.save(observatory) }
    }

    @Test
    fun `Find an observatory by name after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findByName(observatory.name) } returns Optional.of(observatory)
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(newResource)

        service.findByName(observatory.name)

        verify(exactly = 1) { repository.findByName(observatory.name) }
    }

    @Test
    fun `Find an observatory by displayId after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findByDisplayId(observatory.displayId) } returns Optional.of(observatory)
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(newResource)

        service.findByDisplayId(observatory.displayId)

        verify(exactly = 1) { repository.findByDisplayId(observatory.displayId) }
    }

    @Test
    fun `Find an observatory by Id after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()

        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(newResource)

        service.findById(observatory.id)

        verify(exactly = 1) { repository.findById(observatory.id) }
    }

    @Test
    fun `Finding several observatories by research field`() {
        val researchField = createResource(
            classes = setOf(ThingId("ResearchField"))
        )
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID()))).copy(
            researchField = researchField.id
        )
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val researchFieldId = researchField.id
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)

        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
        every { repository.findAllByResearchField(researchFieldId, pageRequest) } returns page

        service.findAllByResearchField(researchFieldId, pageRequest)

        verify(exactly = 1) { repository.findAllByResearchField(researchFieldId, pageRequest) }
    }

    @Test
    fun `Finding several observatories by organization Id`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)
        val organizationId = observatory.organizationIds.single()

        every { repository.findAllByOrganizationId(organizationId, pageRequest) } returns page
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(newResource)

        service.findAllByOrganizationId(organizationId, pageRequest)

        verify(exactly = 1) { repository.findAllByOrganizationId(organizationId, pageRequest) }
    }

    @Test
    fun `Find all observatories`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val newResource = createResource()
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequest.of(0, Int.MAX_VALUE)

        every { repository.findAll(pageRequest) } returns page
        every { resourceRepository.findById(observatory.researchField!!) } returns Optional.of(newResource)

        service.findAll(pageRequest)

        verify(exactly = 1) { repository.findAll(pageRequest) }
    }
}
