package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryAlreadyExists
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.ObservatoryURLNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.pageOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [ObservatoryController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ObservatoryController::class])
@DisplayName("Given an Observatory controller")
internal class ObservatoryControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var observatoryUseCases: ObservatoryUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var resourceUseCases: ResourceUseCases

    @MockkBean
    private lateinit var observatoryRepository: ObservatoryRepository


    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Fetching the observatory after creating, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/${observatory.id}/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
    }

    @Test
    fun `Creating the observatory with duplicate name, status must be 400`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val body = ObservatoryController.CreateObservatoryRequest(
            name = observatory.name,
            organizationId = observatory.organizationIds.single(),
            description = observatory.description!!,
            researchField = observatory.researchField!!,
            displayId = observatory.displayId
        )
        every { observatoryUseCases.findByName(observatory.name) } returns Optional.of(observatory)

        mockMvc.performPost("/api/observatories/", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(ObservatoryAlreadyExists.withName(observatory.name).message))

        verify(exactly = 0) {
            observatoryUseCases.create(any())
        }
    }

    @Test
    fun `Creating the observatory with duplicate displayId, status must be 400`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        val body = ObservatoryController.CreateObservatoryRequest(
            name = "observatoryName",
            organizationId = observatory.organizationIds.single(),
            description = observatory.description!!,
            researchField = observatory.researchField!!,
            displayId = observatory.displayId
        )
        every { observatoryUseCases.findByName("observatoryName") } returns Optional.empty()
        every { observatoryUseCases.findByDisplayId(observatory.displayId) } returns Optional.of(observatory)

        mockMvc.performPost("/api/observatories/", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(ObservatoryAlreadyExists.withDisplayId(observatory.displayId).message))

        verify(exactly = 0) { observatoryUseCases.create(any()) }
    }

    @Test
    fun `Fetching the non existing observatory, status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryUseCases.findById(id) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/$id/"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/"))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
    }

    @Test
    fun `Creating the observatory without existing organization, status must be 404`() {
        val organizationId = OrganizationId(UUID.randomUUID())

        every { observatoryUseCases.findByName(any()) } returns Optional.empty()
        every { observatoryUseCases.findByDisplayId(any()) } returns Optional.empty()
        every { observatoryUseCases.create(any()) } throws OrganizationNotFound(organizationId)

        val body = ObservatoryController.CreateObservatoryRequest(
            name = "test",
            organizationId = organizationId,
            description = "test observatory",
            researchField = ThingId("R45"),
            displayId = "test"
        )

        mockMvc.performPost("/api/observatories/", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(OrganizationNotFound(organizationId).message))
    }

    @Test
    fun `Fetching the observatory with display Id, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        every { observatoryUseCases.findByDisplayId(observatory.displayId) } returns Optional.of(observatory)
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/${observatory.displayId}/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findByDisplayId(observatory.displayId) }
    }

    @Test
    fun `Fetching the observatory with wrong display Id, status must be 404`() {
        val wrongId = "IncorrectId"
        every { observatoryUseCases.findByDisplayId(wrongId) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/$wrongId/"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/IncorrectId/"))
            .andExpect(jsonPath("$.message").value(ObservatoryURLNotFound(wrongId).message))
    }

    @Test
    fun `Fetching all observatories, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory1 = createObservatory(organizationsIds)
        val observatory2 = createObservatory(organizationsIds)
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<Observatory> = pageOf(observatory1, observatory2, pageable = pageable)

        every { observatoryUseCases.findAll(any()) } returns page
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAll(any()) }
    }

    @Test
    fun `Fetching all observatories by name containing, status must be 200 OK`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<Observatory> = pageOf(observatory, pageable = pageable)

        every { observatoryUseCases.findAllByNameContains("Label", any()) } returns page
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/?q=Label"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByNameContains("Label", any()) }
    }

    @Test
    fun `Fetching all observatories with too many parameters, status must be 400 BAD REQUEST`() {
        mockMvc.perform(get("/api/observatories/?q=Label&research_field=R1234"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(TooManyParameters.atMostOneOf("q", "research_field").message))
    }

    @Test
    fun `Fetching all observatories by research field, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory1 = createObservatory(organizationsIds)
        val observatory2 = createObservatory(organizationsIds)
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<Observatory> = pageOf(observatory1, observatory2, pageable = pageable)

        every { observatoryUseCases.findAllByResearchField(observatory1.researchField!!, any()) } returns page
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/?research_field=${observatory1.researchField}"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByResearchField(observatory1.researchField!!, any()) }
    }

    @Test
    fun `Given the observatory name is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val updatedName = "new observatory"
        val body = ObservatoryController.UpdateRequest(updatedName)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeName(observatory.id, updatedName) } just runs
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.performPut("/api/observatories/${observatory.id}/name", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/${observatory.id}/name", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the observatory with invalid name, then status must be 400`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val observatoryId = observatory.id
        val updatedName = ""
        val body = ObservatoryController.UpdateRequest(updatedName)

        every { observatoryUseCases.findById(observatoryId) } returns Optional.of(observatory)
        every { observatoryUseCases.changeName(observatoryId, updatedName) } just runs

        mockMvc.performPut("/api/observatories/$observatoryId/name", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$observatoryId/name"))

        mockMvc.performPost("/api/observatories/$observatoryId/name", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$observatoryId/name"))
    }

    @Test
    fun `Updating the name of non existing observatory, then status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val updatedName = "name"
        val body = ObservatoryController.UpdateRequest(updatedName)

        every { observatoryUseCases.changeName(id, updatedName) } throws ObservatoryNotFound(id)

        mockMvc.performPut("/api/observatories/$id/name", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/name"))

        mockMvc.performPost("/api/observatories/$id/name", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/name"))
    }

    @Test
    fun `Given the observatory description is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id
        val updatedDescription = "new description"
        val body = ObservatoryController.UpdateRequest(updatedDescription)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeDescription(id, updatedDescription) } just runs
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.performPut("/api/observatories/$id/description", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/$id/description", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Given the observatory organization is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id
        val updatedOrganizationId = createOrganization().id
        val body = ObservatoryController.UpdateOrganizationRequest(updatedOrganizationId!!)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.addOrganization(id, updatedOrganizationId) } just runs
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.performPut("/api/observatories/add/$id/organization", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/add/$id/organization", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the organization of non existing observatory, then status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val updatedOrganizationId = OrganizationId(UUID.randomUUID())
        val body = ObservatoryController.UpdateOrganizationRequest(updatedOrganizationId)

        every { observatoryUseCases.addOrganization(id, updatedOrganizationId) } throws ObservatoryNotFound(id)

        mockMvc.performPut("/api/observatories/add/$id/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/add/$id/organization"))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))

        mockMvc.performPost("/api/observatories/add/$id/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/add/$id/organization"))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
    }

    @Test
    fun `Updating the observatory with no existing organization, then status must be 404`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        val organizationId = OrganizationId(UUID.randomUUID())
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every {
            observatoryUseCases.addOrganization(observatory.id, organizationId)
        } throws OrganizationNotFound(organizationId)

        mockMvc.performPut("/api/observatories/add/${observatory.id}/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/add/${observatory.id}/organization"))
            .andExpect(jsonPath("$.message").value(OrganizationNotFound(organizationId).message))

        mockMvc.performPost("/api/observatories/add/${observatory.id}/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/add/${observatory.id}/organization"))
            .andExpect(jsonPath("$.message").value(OrganizationNotFound(organizationId).message))
    }

    @Test
    fun `Updating the description of non existing observatory, then status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val updatedDescription = "description"
        val body = ObservatoryController.UpdateRequest(updatedDescription)

        every { observatoryUseCases.changeDescription(id, updatedDescription) } throws ObservatoryNotFound(id)

        mockMvc.performPut("/api/observatories/$id/description", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/description"))

        mockMvc.performPost("/api/observatories/$id/description", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/description"))
    }

    @Test
    fun `Updating the observatory with invalid description, the status is 400`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id
        val updatedDescription = ""
        val body = ObservatoryController.UpdateRequest(updatedDescription)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeDescription(id, updatedDescription) } just runs

        mockMvc.performPut("/api/observatories/$id/description", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/description"))

        mockMvc.performPost("/api/observatories/$id/description", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/description"))
    }

    @Test
    fun `Given the observatory research field is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id
        val newResource = createResource()
        val newResearchField = newResource.id
        val body = ObservatoryController.UpdateRequest(newResearchField.value)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeResearchField(id, newResearchField) } just runs
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.performPut("/api/observatories/$id/research_field", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/$id/research_field", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the observatory with wrong research field, status is 400`() {
        val id = ObservatoryId(UUID.randomUUID())
        val newResearchField = ResearchField("", "")
        val body = ObservatoryController.UpdateRequest(newResearchField.id!!)

        mockMvc.performPut("/api/observatories/$id/research_field", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/research_field"))

        mockMvc.performPost("/api/observatories/$id/research_field", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/research_field"))
    }

    @Test
    fun `Updating the research field of non existing observatory, status is 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val researchFieldId = ThingId("R1234")
        val body = ObservatoryController.UpdateRequest(researchFieldId.value)

        every { observatoryUseCases.changeResearchField(id, researchFieldId) } throws ObservatoryNotFound(id)

        mockMvc.performPut("/api/observatories/$id/research_field", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/research_field"))

        mockMvc.performPost("/api/observatories/$id/research_field", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/research_field"))
    }

    @Test
    fun `Given the observatory id, delete the linked organization, when service succeeds, then status is 200 OK`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id
        val organizationId = organizationsIds.single()
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.deleteOrganization(id, organizationId) } just runs
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.performPut("/api/observatories/delete/$id/organization", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/delete/$id/organization", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Given the observatory id, delete the linked organization without the existence of observatory, then status is 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.deleteOrganization(id, organizationId) } throws ObservatoryNotFound(id)

        mockMvc.performPut("/api/observatories/delete/$id/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/delete/$id/organization"))

        mockMvc.performPost("/api/observatories/delete/$id/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/delete/$id/organization"))
    }

    @Test
    fun `Given the observatory id, delete the non existing organization, then status is 404`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        val organizationId = OrganizationId(UUID.randomUUID())
        // deleting non-existing organization
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryUseCases.deleteOrganization(observatory.id, organizationId) } throws OrganizationNotFound(organizationId)

        mockMvc.performPut("/api/observatories/delete/${observatory.id}/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(OrganizationNotFound(organizationId).message))
            .andExpect(jsonPath("$.path").value("/api/observatories/delete/${observatory.id}/organization"))

        mockMvc.performPost("/api/observatories/delete/${observatory.id}/organization", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/delete/${observatory.id}/organization"))
            .andExpect(jsonPath("$.message").value(OrganizationNotFound(organizationId).message))
    }

    @Test
    fun `When fetching all research fields used in observatories, status must be 200`() {
        val id = ThingId("R123")
        val label = "fancy research field"
        val pageable = PageRequest.of(0, 10)
        val page: Page<ThingId> = pageOf(id, pageable = pageable)
        val resource = createResource(
            id = id,
            label = label,
            classes = setOf(ThingId("ResearchField"))
        )

        every { observatoryUseCases.findAllResearchFields(any()) } returns page
        every { resourceUseCases.findById(id) } returns Optional.of(resource)

        mockMvc.perform(get("/api/observatories/research-fields"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(id.value))
            .andExpect(jsonPath("$.content[0].label").value(label))

        verify(exactly = 1) { observatoryUseCases.findAllResearchFields(any()) }
    }

    private fun MockMvc.performPut(urlTemplate: String, updateRequest: Any): ResultActions = perform(
        put(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(updateRequest))
    )

    private fun MockMvc.performPost(urlTemplate: String, updateRequest: Any): ResultActions = perform(
        post(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(updateRequest))
    )
}
