package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ContributorService
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase.*
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.UpdateResourceUseCase
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import io.mockk.every
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.*
import java.util.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
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

@ContextConfiguration(classes = [ResourceController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [ResourceController::class])
@DisplayName("Given a Resource controller")
internal class ResourceControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @Suppress("unused") // required by ResourceController but not used in the test (yet)
    @MockkBean
    private lateinit var contributorService: ContributorService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var templateRepository: TemplateRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given the contributors are requested, when service succeeds, then status is 200 OK and contributors are returned`() {
        val id = ThingId("R123")
        val contributorIds = listOf(
            ContributorId(UUID.randomUUID()),
            ContributorId(UUID.randomUUID())
        )
        val contributors = PageImpl(
            contributorIds,
            PageRequest.of(0, 25),
            contributorIds.size.toLong()
        )
        every { resourceService.findAllContributorsByResourceId(id, any()) } returns contributors

        mockMvc.perform(get("/api/resources/$id/contributors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun `Given the contributors are requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findAllContributorsByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        mockMvc.perform(get("/api/resources/$id/contributors"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/contributors"))
    }

    @Test
    fun `Given a timeline is requested, when service succeeds, then status is 200 OK and timeline is returned`() {
        val id = ThingId("R123")
        val resourceContributors = listOf(
            UUID.randomUUID() to OffsetDateTime.now(),
            UUID.randomUUID() to OffsetDateTime.now()
        ).map {
            ResourceContributor(it.first.toString(), it.second.format(ISO_DATE_TIME))
        }
        val timeline = PageImpl(
            resourceContributors,
            PageRequest.of(0, 25),
            resourceContributors.size.toLong()
        )
        every { resourceService.findTimelineByResourceId(id, any()) } returns timeline

        mockMvc.perform(get("/api/resources/$id/timeline"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun `Given a timeline is requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findTimelineByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        mockMvc.perform(get("/api/resources/$id/timeline"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/timeline"))
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `When creating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val command = CreateResourceRequest(
            id = null,
            label = "irrelevant",
            classes = setOf(ThingId("List"))
        )
        val exception = InvalidClassCollection(command.classes)

        every { contributorService.findById(any()) } returns Optional.of(
            Contributor(
                id = ContributorId(UUID.randomUUID()),
                name = "irrelevant",
                joinedAt = OffsetDateTime.now()
            )
        )
        every { resourceService.create(any<CreateCommand>()) } throws exception

        mockMvc.performPost("/api/resources/", command)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/"))
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun `When updating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            classes = setOf(ThingId("List"))
        )
        val exception = InvalidClassCollection(command.classes!!)

        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { resourceService.update(command) } throws exception

        mockMvc.performPut("/api/resources/${resource.id}", command)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/${resource.id}"))
    }

    private fun MockMvc.performPost(urlTemplate: String, request: Any): ResultActions = perform(
        post(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(request))
    )

    private fun MockMvc.performPut(urlTemplate: String, request: Any): ResultActions = perform(
        put(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(request))
    )
}
