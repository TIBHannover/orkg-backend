package org.orkg.community.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.input.UpdateObservatoryUseCase.UpdateCommand
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class ObservatoryServiceUnitTests {

    private val repository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val service = ObservatoryService(repository, organizationRepository, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(repository, organizationRepository, resourceRepository)
    }

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
        verify(exactly = 1) { resourceRepository.findById(observatory.researchField!!) }
        verify(exactly = 1) { organizationRepository.findById(organizationId) }
    }

    @Test
    fun `Creating an observatory without existing organization`() {
        val oId = OrganizationId(UUID.randomUUID())
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

        verify(exactly = 1) { organizationRepository.findById(oId) }
    }

    @Test
    fun `Find an observatory by name after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        every { repository.findByName(observatory.name) } returns Optional.of(observatory)

        service.findByName(observatory.name)

        verify(exactly = 1) { repository.findByName(observatory.name) }
    }

    @Test
    fun `Find an observatory by displayId after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        every { repository.findByDisplayId(observatory.displayId) } returns Optional.of(observatory)

        service.findByDisplayId(observatory.displayId)

        verify(exactly = 1) { repository.findByDisplayId(observatory.displayId) }
    }

    @Test
    fun `Find an observatory by Id after creating it`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        every { repository.findById(observatory.id) } returns Optional.of(observatory)

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
        val pageRequest = PageRequests.ALL

        every { resourceRepository.findById(researchFieldId) } returns Optional.of(researchField)
        every { repository.findAllByResearchField(researchFieldId, pageRequest) } returns page

        service.findAllByResearchField(researchFieldId, pageRequest)

        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
        verify(exactly = 1) { repository.findAllByResearchField(researchFieldId, pageRequest) }
    }

    @Test
    fun `Finding several observatories by organization Id`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequests.ALL
        val organizationId = observatory.organizationIds.single()

        every { repository.findAllByOrganizationId(organizationId, pageRequest) } returns page

        service.findAllByOrganizationId(organizationId, pageRequest)

        verify(exactly = 1) { repository.findAllByOrganizationId(organizationId, pageRequest) }
    }

    @Test
    fun `Find all observatories`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val page: Page<Observatory> = PageImpl(listOf(observatory))
        val pageRequest = PageRequests.ALL

        every { repository.findAll(pageRequest) } returns page

        service.findAll(pageRequest)

        verify(exactly = 1) { repository.findAll(pageRequest) }
    }

    @Test
    fun `Given an observatory update command, when updating all fields, it returns success and observatory is updated`() {
        val observatory = createObservatory()
        val organization = createOrganization()
        val researchField = createResource(ThingId("R456"), classes = setOf(Classes.researchField))
        val command = UpdateCommand(
            id = observatory.id,
            name = "new name",
            organizations = setOf(organization.id!!),
            description = "new description",
            researchField = researchField.id
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)
        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe observatory.id
                it.name shouldBe command.name
                it.organizationIds shouldBe command.organizations
                it.description shouldBe command.description
                it.researchField shouldBe command.researchField
                it.displayId shouldBe observatory.displayId
            })
        }
    }

    @Test
    fun `Given an observatory update command, when updating the name, it returns success and observatory is updated`() {
        val observatory = createObservatory()
        val command = UpdateCommand(
            id = observatory.id,
            name = "new name"
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe observatory.id
                it.name shouldBe command.name
                it.organizationIds shouldBe observatory.organizationIds
                it.description shouldBe observatory.description
                it.researchField shouldBe observatory.researchField
                it.displayId shouldBe observatory.displayId
            })
        }
    }

    @Test
    fun `Given an observatory update command, when updating the organizations, it returns success and observatory is updated`() {
        val observatory = createObservatory()
        val organization = createOrganization()
        val command = UpdateCommand(
            id = observatory.id,
            organizations = setOf(organization.id!!),
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { organizationRepository.findById(organization.id!!) } returns Optional.of(organization)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { organizationRepository.findById(organization.id!!) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe observatory.id
                it.name shouldBe observatory.name
                it.organizationIds shouldBe command.organizations
                it.description shouldBe observatory.description
                it.researchField shouldBe observatory.researchField
                it.displayId shouldBe observatory.displayId
            })
        }
    }

    @Test
    fun `Given an observatory update command, when updating the description, it returns success and observatory is updated`() {
        val observatory = createObservatory()
        val command = UpdateCommand(
            id = observatory.id,
            description = "new description",
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe observatory.id
                it.name shouldBe observatory.name
                it.organizationIds shouldBe observatory.organizationIds
                it.description shouldBe command.description
                it.researchField shouldBe observatory.researchField
                it.displayId shouldBe observatory.displayId
            })
        }
    }

    @Test
    fun `Given an observatory update command, when updating the research field, it returns success and observatory is updated`() {
        val observatory = createObservatory()
        val researchField = createResource(ThingId("R456"), classes = setOf(Classes.researchField))
        val command = UpdateCommand(
            id = observatory.id,
            researchField = researchField.id
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe observatory.id
                it.name shouldBe observatory.name
                it.organizationIds shouldBe observatory.organizationIds
                it.description shouldBe observatory.description
                it.researchField shouldBe command.researchField
                it.displayId shouldBe observatory.displayId
            })
        }
    }

    @Test
    fun `Given an observatory update command, when command has no contents, it does nothing`() {
        val command = UpdateCommand(ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33"))
        service.update(command)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `Given an observatory update command, when observatory does not exist, it throws an exception`() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val command = UpdateCommand(id, name = "new name")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ObservatoryNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given an observatory update command, when organization does not exist, it throws an exception`() {
        val observatory = createObservatory()
        val organizationId = OrganizationId("d02073bc-30fd-481e-9167-f3fc3595d590")
        val command = UpdateCommand(
            id = observatory.id,
            organizations = setOf(organizationId),
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { organizationRepository.findById(organizationId) } returns Optional.empty()

        assertThrows<OrganizationNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { organizationRepository.findById(organizationId) }
    }

    @Test
    fun `Given an observatory update command, when research field does not exist, it throws an exception`() {
        val observatory = createObservatory()
        val researchFieldId = ThingId("R456")
        val command = UpdateCommand(
            id = observatory.id,
            researchField = researchFieldId
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceRepository.findById(researchFieldId) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { resourceRepository.findById(researchFieldId) }
    }

    @Test
    fun `Given an observatory update command, when research field resource exists, but is not a research field instance, it throws an exception`() {
        val observatory = createObservatory()
        val researchField = createResource(ThingId("R456"))
        val command = UpdateCommand(
            id = observatory.id,
            researchField = researchField.id
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceRepository.findById(researchField.id) } returns Optional.of(researchField)

        assertThrows<ResearchFieldNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(observatory.id) }
        verify(exactly = 1) { resourceRepository.findById(researchField.id) }
    }

    @Test
    fun `Given an observatory update command, when new fields are equal, it skips validation and does not save the observatory again`() {
        val organization = createOrganization()
        val observatory = createObservatory(organizationIds = setOf(organization.id!!))
        val command = UpdateCommand(
            id = observatory.id,
            name = observatory.name,
            organizations = observatory.organizationIds,
            description = observatory.description,
            researchField = observatory.researchField
        )
        every { repository.findById(observatory.id) } returns Optional.of(observatory)

        service.update(command)

        verify(exactly = 1) { repository.findById(observatory.id) }
    }
}
