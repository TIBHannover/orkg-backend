package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.application.ObservatoryAlreadyExists
import eu.tib.orkg.prototype.community.application.ObservatoryController
import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.application.ObservatoryURLNotFound
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.createObservatory
import eu.tib.orkg.prototype.createOrganization
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.StatisticsService
import eu.tib.orkg.prototype.statements.services.toResourceRepresentation
import eu.tib.orkg.prototype.statements.spi.ObservatoryResources
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [ObservatoryController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given an ObservatoryController controller")
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
    private lateinit var userRepository: UserRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: ContributorService

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var statisticsService: StatisticsService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Fetching the observatory after creating, status must be 200`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        every { observatoryUseCases.findById(observatory.id!!) } returns Optional.of(observatory)

        mockMvc.perform(get("/api/observatories/${observatory.id}/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id!!) }
    }

    @Test
    fun `Creating the observatory with duplicate name, status must be 400`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val body = ObservatoryController.CreateObservatoryRequest(
            observatoryName = observatory.name!!,
            organizationId = observatory.organizationIds.single(),
            description = observatory.description!!,
            researchField = ThingId(observatory.researchField?.id!!),
            displayId = observatory.displayId!!
        )
        every { observatoryUseCases.findByName(observatory.name!!) } returns Optional.of(observatory)

        mockMvc.performPost("/api/observatories/", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(ObservatoryAlreadyExists.withName(observatory.name!!).message))

        verify(exactly = 0) {
            observatoryUseCases.create(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Creating the observatory with duplicate displayId, status must be 400`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)

        val body = ObservatoryController.CreateObservatoryRequest(
            observatoryName = "observatoryName",
            organizationId = observatory.organizationIds.single(),
            description = observatory.description!!,
            researchField = ThingId(observatory.researchField?.id!!),
            displayId = observatory.displayId!!
        )
        every { observatoryUseCases.findByName("observatoryName") } returns Optional.empty()
        every { observatoryUseCases.findByDisplayId(observatory.displayId!!) } returns Optional.of(observatory)

        mockMvc.performPost("/api/observatories/", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/observatories/"))
            .andExpect(jsonPath("$.message").value(ObservatoryAlreadyExists.withDisplayId(observatory.displayId!!).message))

        verify(exactly = 0) { observatoryUseCases.create(any(), any(), any(), any(), any(), any()) }
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
        every {
            observatoryUseCases.create(any(), any(), any(), organizationId, any(), any())
        } throws OrganizationNotFound(organizationId)

        val body = ObservatoryController.CreateObservatoryRequest(
            observatoryName = "test",
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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)

        every { observatoryUseCases.findByDisplayId(observatory.displayId!!) } returns Optional.of(observatory)

        mockMvc.perform(get("/api/observatories/${observatory.displayId}/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.findByDisplayId(observatory.displayId!!) }
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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory1 = createObservatory(organizationsIds)
        val observatory2 = createObservatory(organizationsIds)
        val pageable = PageRequest.of(0, Int.MAX_VALUE)
        val page: Page<Observatory> = PageImpl(listOf(observatory1, observatory2))

        every { observatoryUseCases.listObservatories(pageable) } returns page

        mockMvc.perform(get("/api/observatories/"))
            .andExpect(status().isOk)

        verify(exactly = 1) { observatoryUseCases.listObservatories(pageable) }
    }

    @Test
    fun `Given the observatory name is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val updatedName = "new observatory"
        val body = ObservatoryController.UpdateRequest(updatedName)
        val updatedObservatory = observatory.copy(name = updatedName)

        every { observatoryUseCases.findById(observatory.id!!) } returns Optional.of(observatory)
        every { observatoryUseCases.changeName(observatory.id!!, updatedName) } returns updatedObservatory

        mockMvc.performPut("/api/observatories/${observatory.id}/name", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/${observatory.id}/name", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the observatory with invalid name, then status must be 400`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val observatoryId = observatory.id
        val updatedName = ""
        val body = ObservatoryController.UpdateRequest(updatedName)
        val updatedObservatory = observatory.copy(name = updatedName)
        every { observatoryUseCases.findById(observatoryId!!) } returns Optional.of(observatory)
        every { observatoryUseCases.changeName(observatoryId!!, updatedName) } returns updatedObservatory

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
        every { observatoryUseCases.findById(id) } returns Optional.empty()

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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
        val updatedDescription = "new description"
        val body = ObservatoryController.UpdateRequest(updatedDescription)
        val updatedObservatory = observatory.copy(description = updatedDescription)
        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeDescription(id, updatedDescription) } returns updatedObservatory

        mockMvc.performPut("/api/observatories/$id/description", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/$id/description", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Given the observatory organization is replaced, when service succeeds, then status is 200 OK and observatory is returned`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
        val updatedOrganizationId = createOrganization().id
        val body = ObservatoryController.UpdateOrganizationRequest(updatedOrganizationId!!)
        val updatedObservatory = observatory.copy(organizationIds = setOf(updatedOrganizationId))

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.addOrganization(id, updatedOrganizationId) } returns updatedObservatory

        mockMvc.performPut("/api/observatories/add/$id/organization", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/add/$id/organization", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the organization of non existing observatory, then status must be 404`() {
        val id = ObservatoryId(UUID.randomUUID())
        val updatedOrganizationId = createOrganization().id
        val body = ObservatoryController.UpdateOrganizationRequest(updatedOrganizationId!!)
        every { observatoryUseCases.findById(id) } returns Optional.empty()

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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)

        val organizationId = OrganizationId(UUID.randomUUID())
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)
        every { observatoryUseCases.findById(observatory.id!!) } returns Optional.of(observatory)
        every {
            observatoryUseCases.addOrganization(observatory.id!!, organizationId)
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
        every { observatoryUseCases.findById(id) } returns Optional.empty()

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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
        val updatedDescription = ""
        val body = ObservatoryController.UpdateRequest(updatedDescription)
        val updatedObservatory = observatory.copy(description = updatedDescription)
        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.changeDescription(id, updatedDescription) } returns updatedObservatory

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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
        val newResource = createResource().toResourceRepresentation(mapOf(), emptyMap())
        val newResearchField = ResearchField(newResource.id.value, newResource.label)
        val body = ObservatoryController.UpdateRequest(newResearchField.id!!)
        val updatedObservatory = observatory.copy(researchField = newResearchField)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { resourceService.findById(newResource.id) } returns Optional.of(newResource)
        every { observatoryUseCases.changeResearchField(id, newResearchField) } returns updatedObservatory

        mockMvc.performPut("/api/observatories/$id/research_field", body)
            .andExpect(status().isOk)

        mockMvc.performPost("/api/observatories/$id/research_field", body)
            .andExpect(status().isOk)
    }

    @Test
    fun `Updating the observatory with wrong research field, status is 400`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
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
        val newResource = createResource().toResourceRepresentation(mapOf(), emptyMap())
        val newResearchField = ResearchField(newResource.id.value, newResource.label)
        val body = ObservatoryController.UpdateRequest(value = newResearchField.id!!)
        every { observatoryUseCases.findById(id) } returns Optional.empty()
        every { resourceService.findById(newResource.id) } returns Optional.of(newResource)

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
    fun `Given the observatory id, when service succeeds, then status is 200 OK and papers list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val paperResource = createResource().copy(observatoryId = id, classes = setOf(ThingId("Paper")))
        every {
            resourceService.findPapersByObservatoryId(id = id)
        } returns listOf(paperResource.toResourceRepresentation(mapOf(), emptyMap()))

        mockMvc.perform(get("/api/observatories/$id/papers"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findPapersByObservatoryId(id) }
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and comparisons list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val comparisonResource = createResource().copy(observatoryId = id, classes = setOf(ThingId("Comparison")))
        every {
            resourceService.findComparisonsByObservatoryId(id = id)
        } returns listOf(comparisonResource.toResourceRepresentation(mapOf(), emptyMap()))

        mockMvc.perform(get("/api/observatories/$id/comparisons"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findComparisonsByObservatoryId(id) }
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and problems list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val problemResource = createResource().copy(
            observatoryId = id,
            classes = setOf(ThingId("Problem"))
        ).toResourceRepresentation(mapOf(), emptyMap())
        val pageable = PageRequest.of(0, 20)

        every {
            resourceService.findProblemsByObservatoryId(id, any())
        } returns PageImpl(listOf(problemResource), pageable, 1)

        mockMvc.perform(get("/api/observatories/$id/problems"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findProblemsByObservatoryId(id, any()) }
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryResources(id.value.toString(), 1, 0)
        every { statisticsService.getObservatoriesPapersAndComparisonsCount() } returns listOf(response)

        mockMvc.perform(get("/api/observatories/stats/observatories"))
            .andExpect(status().isOk)

        verify(exactly = 1) { statisticsService.getObservatoriesPapersAndComparisonsCount() }
    }

    @Test
    fun `Given the observatory id, delete the linked organization, when service succeeds, then status is 200 OK`() {
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)
        val id = observatory.id!!
        val organizationId = organizationsIds.single()
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.findById(id) } returns Optional.of(observatory)
        every { observatoryUseCases.deleteOrganization(id, organizationId) } returns observatory

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
        every { observatoryUseCases.findById(id) } returns Optional.empty()

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
        val organizationsIds = (0..1).map { createOrganization().id!! }.toSet()
        val observatory = createObservatory(organizationsIds)

        val organizationId = OrganizationId(UUID.randomUUID())
        // deleting non-existing organization
        val body = ObservatoryController.UpdateOrganizationRequest(organizationId)

        every { observatoryUseCases.findById(observatory.id!!) } returns Optional.of(observatory)
        every { observatoryUseCases.deleteOrganization(observatory.id!!, organizationId) } throws OrganizationNotFound(organizationId)

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
