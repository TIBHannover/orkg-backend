package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.thingIdConstraint
import org.orkg.community.adapter.input.rest.ObservatoryController.CreateObservatoryRequest
import org.orkg.community.adapter.input.rest.ObservatoryController.UpdateObservatoryRequest
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryAlreadyExists
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.testing.fixtures.configuration.CommunityControllerUnitTestConfiguration
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import java.util.UUID

@ContextConfiguration(classes = [ObservatoryController::class, CommunityControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ObservatoryController::class])
internal class ObservatoryControllerUnitTest : MockMvcBaseTest("observatories") {
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

        get("/api/observatories/{id}", observatory.id)
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { resourceUseCases.findById(any()) }
    }

    @Test
    fun `Fetching the non existing observatory, status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())

        every { observatoryUseCases.findById(id) } returns Optional.empty()

        get("/api/observatories/{id}", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(id) }
    }

    @Test
    @TestWithMockCurator
    fun `Creating the observatory without existing organization, status must be 404`() {
        val organizationId = OrganizationId(UUID.randomUUID())

        every { observatoryUseCases.create(any()) } throws OrganizationNotFound(organizationId)

        val body = CreateObservatoryRequest(
            name = "test",
            organizationId = organizationId,
            description = "test observatory",
            researchField = ThingId("R45"),
            displayId = "test",
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"))
        )

        post("/api/observatories")
            .content(body)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")

        verify(exactly = 1) { observatoryUseCases.create(any()) }
    }

    @Test
    fun `Fetching the observatory with display Id, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory = createObservatory(organizationsIds)

        every { observatoryUseCases.findByDisplayId(observatory.displayId) } returns Optional.of(observatory)
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        get("/api/observatories/{id}", observatory.displayId)
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findByDisplayId(observatory.displayId) }
        verify(exactly = 1) { resourceUseCases.findById(any()) }
    }

    @Test
    fun `Fetching the observatory with wrong display Id, status must be 404`() {
        val wrongId = "IncorrectId"
        every { observatoryUseCases.findByDisplayId(wrongId) } returns Optional.empty()

        get("/api/observatories/{id}", wrongId)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { observatoryUseCases.findByDisplayId(wrongId) }
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

        get("/api/observatories")
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAll(any()) }
        verify(exactly = 2) { resourceUseCases.findById(any()) }
    }

    @Test
    fun `Fetching all observatories by name containing, status must be 200 OK`() {
        val observatory = createObservatory(setOf(OrganizationId(UUID.randomUUID())))
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<Observatory> = pageOf(observatory, pageable = pageable)

        every { observatoryUseCases.findAllByNameContains("Label", any()) } returns page
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        get("/api/observatories")
            .param("q", "Label")
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByNameContains("Label", any()) }
        verify(exactly = 1) { resourceUseCases.findById(any()) }
    }

    @Test
    fun `Fetching all observatories with too many parameters, status must be 400 BAD REQUEST`() {
        get("/api/observatories")
            .param("q", "Label")
            .param("research_field", "R1234")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
    }

    @Test
    fun `Fetching all observatories by research field, status must be 200`() {
        val organizationsIds = setOf(OrganizationId(UUID.randomUUID()))
        val observatory1 = createObservatory(organizationsIds)
        val observatory2 = createObservatory(organizationsIds)
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<Observatory> = pageOf(observatory1, observatory2, pageable = pageable)

        every { observatoryUseCases.findAllByResearchFieldId(observatory1.researchField!!, any()) } returns page
        every { resourceUseCases.findById(any()) } returns Optional.empty()

        get("/api/observatories")
            .param("research_field", observatory1.researchField!!.value)
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findAllByResearchFieldId(observatory1.researchField!!, any()) }
        verify(exactly = 2) { resourceUseCases.findById(any()) }
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

        get("/api/observatories/research-fields")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(id.value))
            .andExpect(jsonPath("$.content[0].label").value(label))

        verify(exactly = 1) { observatoryUseCases.findAllResearchFields(any()) }
        verify(exactly = 1) { resourceUseCases.findById(id) }
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
        val request = CreateObservatoryRequest(
            name = observatory.name,
            organizationId = observatory.organizationIds.first(),
            description = observatory.description!!,
            researchField = observatory.researchField!!,
            displayId = observatory.displayId,
            sustainableDevelopmentGoals = observatory.sustainableDevelopmentGoals
        )

        every { observatoryUseCases.create(any()) } returns observatory.id

        documentedPostRequestTo("/api/observatories")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/observatories/$id")))
            .andDocument {
                summary("Creating observatories")
                description(
                    """
                    A `POST` request creates a new observatory with the given parameters.
                    The response will be `201 Created` when successful.
                    The observatory can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created observatory can be fetched from."),
                )
                requestFields<CreateObservatoryRequest>(
                    fieldWithPath("name").description("The name of the observatory. Alternatively, the legacy field `observatory_name` can be used for equivalent behavior."),
                    fieldWithPath("organization_id").description("The id of the organization that the observatory belongs to."),
                    fieldWithPath("description").description("The description of the observatory."),
                    fieldWithPath("research_field").description("The id of the research field of the observatory."),
                    fieldWithPath("display_id").description("The URI slug of the observatory."),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the observatory will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                )
                throws(
                    ObservatoryAlreadyExists::class,
                    OrganizationNotFound::class,
                    ResearchFieldNotFound::class,
                    SustainableDevelopmentGoalNotFound::class,
                )
            }

        verify(exactly = 1) {
            observatoryUseCases.create(
                withArg {
                    it.id shouldBe null
                    it.name shouldBe observatory.name
                    it.organizations shouldBe observatory.organizationIds
                    it.description shouldBe observatory.description
                    it.researchField shouldBe observatory.researchField
                    it.displayId shouldBe observatory.displayId
                    it.sustainableDevelopmentGoals shouldBe observatory.sustainableDevelopmentGoals
                }
            )
        }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given an observatory, when updated, then status is 204 NO CONTENT")
    fun update() {
        val id = ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
        val request = UpdateObservatoryRequest(
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
            .andDocument {
                summary("Updating observatories")
                description(
                    """
                    A `PATCH` request updates an existing observatory with the given parameters.
                    Only fields provided in the request, and therefore non-null, will be updated.
                    The response will be `204 No Content` when successful.
                    The updated observatory (object) can be retrieved by following the URI in the `Location` header field.
                    
                    NOTE: This endpoint can only be accessed by curators.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created observatory can be fetched from."),
                )
                requestFields<UpdateObservatoryRequest>(
                    fieldWithPath("name").description("The new name of the observatory. (optional)").optional(),
                    fieldWithPath("organizations").description("The new set of organizations that the observatory belongs to. (optional)").optional(),
                    fieldWithPath("description").description("The new description of the observatory. (optional)").optional(),
                    fieldWithPath("research_field").description("The id of the new research field of the observatory. (optional)").optional(),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the observatory will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                )
                throws(
                    ObservatoryNotFound::class,
                    OrganizationNotFound::class,
                    ResearchFieldNotFound::class,
                    SustainableDevelopmentGoalNotFound::class,
                )
            }

        verify(exactly = 1) {
            observatoryUseCases.update(
                withArg {
                    it.id shouldBe id
                    it.name shouldBe "updated"
                    it.organizations shouldBe setOf(OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"))
                    it.description shouldBe "new observatory description"
                    it.researchField shouldBe ThingId("R123")
                    it.sustainableDevelopmentGoals shouldBe setOf(ThingId("SDG1"))
                }
            )
        }
    }
}
