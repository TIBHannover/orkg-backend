package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.ObservatoryURLNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectObservatory
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedPatchRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import orkg.orkg.community.testing.fixtures.observatoryResponseFields

@ContextConfiguration(classes = [ObservatoryController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ObservatoryController::class])
@DisplayName("Given an Observatory controller")
internal class ObservatoryControllerUnitTest : RestDocsTest("observatories") {

    @MockkBean
    private lateinit var observatoryUseCases: ObservatoryUseCases

    @MockkBean
    private lateinit var resourceUseCases: ResourceUseCases

    @MockkBean
    private lateinit var observatoryRepository: ObservatoryRepository

    @Test
    fun `Fetching the observatory after creating, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/${observatory.id}"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
    }

    @Test
    fun `Fetching the non existing observatory, status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryUseCases.findById(id) } returns Optional.empty()

        mockMvc.perform(get("/api/observatories/$id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/$id"))
            .andExpect(jsonPath("$.message").value(ObservatoryNotFound(id).message))
    }

    @Test
    @TestWithMockCurator
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
            displayId = "test",
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"))
        )

        mockMvc.performPost("/api/observatories", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories"))
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

        mockMvc.perform(get("/api/observatories/$wrongId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/observatories/IncorrectId"))
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

        mockMvc.perform(get("/api/observatories"))
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

        mockMvc.perform(get("/api/observatories?q=Label"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByNameContains("Label", any()) }
    }

    @Test
    fun `Fetching all observatories with too many parameters, status must be 400 BAD REQUEST`() {
        mockMvc.perform(get("/api/observatories?q=Label&research_field=R1234"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories"))
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

        mockMvc.perform(get("/api/observatories?research_field=${observatory1.researchField}"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByResearchField(observatory1.researchField!!, any()) }
    }

    @Test
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
    @TestWithMockCurator
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
            classes = setOf(Classes.researchField)
        )

        every { observatoryUseCases.findAllResearchFields(any()) } returns page
        every { resourceUseCases.findById(id) } returns Optional.of(resource)

        mockMvc.perform(get("/api/observatories/research-fields"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(id.value))
            .andExpect(jsonPath("$.content[0].label").value(label))

        verify(exactly = 1) { observatoryUseCases.findAllResearchFields(any()) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given an observatory is created, when service succeeds, then status is 201 CREATED and observatory is returned")
    fun create() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val sdg = ThingId("SDG1")
        val observatory = createObservatory(
            id = id,
            organizationIds = setOf(OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")),
            sustainableDevelopmentGoals = setOf(sdg)
        )
        val researchField = createResource(observatory.researchField!!, classes = setOf(Classes.researchField))
        val request = ObservatoryController.CreateObservatoryRequest(
            name = observatory.name,
            organizationId = observatory.organizationIds.first(),
            description = observatory.description!!,
            researchField = observatory.researchField!!,
            displayId = observatory.displayId,
            sustainableDevelopmentGoals = observatory.sustainableDevelopmentGoals
        )

        every { observatoryUseCases.create(any()) } returns observatory.id
        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { resourceUseCases.findById(observatory.researchField!!) } returns Optional.of(researchField)

        documentedPostRequestTo("/api/observatories")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/observatories/$id")))
            .andExpectObservatory()
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("name").description("The name of the observatory. Alternatively, the legacy field `observatory_name` can be used for equivalent behavior."),
                        fieldWithPath("organization_id").description("The id of the organization that the observatory belongs to."),
                        fieldWithPath("description").description("The description of the observatory."),
                        fieldWithPath("research_field").description("The id of the research field of the observatory."),
                        fieldWithPath("display_id").description("The URI slug of the observatory."),
                        fieldWithPath("sdgs").description("The set of ids of https://sdgs.un.org[sustainable development goals,window=_blank] the observatory belongs to."),
                    ),
                    responseFields(observatoryResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            observatoryUseCases.create(withArg {
                it.id shouldBe null
                it.name shouldBe observatory.name
                it.organizations shouldBe observatory.organizationIds
                it.description shouldBe observatory.description
                it.researchField shouldBe observatory.researchField
                it.displayId shouldBe observatory.displayId
                it.sustainableDevelopmentGoals shouldBe observatory.sustainableDevelopmentGoals
            })
        }
        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { resourceUseCases.findById(observatory.researchField!!) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given an observatory, when updated, then status is 204 NO CONTENT")
    fun update() {
        val id = ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
        val request = ObservatoryController.UpdateObservatoryRequest(
            name = "updated",
            organizations = setOf(OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")),
            description = "new observatory description",
            researchField = ThingId("R123"),
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"))
        )

        every { observatoryUseCases.update(any()) } just runs

        documentedPatchRequestTo("/api/observatories/{id}", id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/observatories/$id")))
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("name").description("The new name of the observatory. (optional)"),
                        fieldWithPath("organizations").description("The new set of organizations that the observatory belongs to. (optional)"),
                        fieldWithPath("description").description("The new description of the observatory. (optional)"),
                        fieldWithPath("research_field").description("The id of the new research field of the observatory. (optional)"),
                        fieldWithPath("sdgs").description("The new set of ids of https://sdgs.un.org/[sustainable development goals,window=_blank] that the observatory belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            observatoryUseCases.update(withArg {
                it.id shouldBe id
                it.name shouldBe "updated"
                it.organizations shouldBe setOf(OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"))
                it.description shouldBe "new observatory description"
                it.researchField shouldBe ThingId("R123")
                it.sustainableDevelopmentGoals shouldBe setOf(ThingId("SDG1"))
            })
        }
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
