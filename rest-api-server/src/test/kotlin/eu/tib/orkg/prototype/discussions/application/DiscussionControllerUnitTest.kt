package eu.tib.orkg.prototype.discussions.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.domain.UserService
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createUser
import eu.tib.orkg.prototype.discussions.api.CreateDiscussionCommentUseCase
import eu.tib.orkg.prototype.discussions.api.DiscussionUseCases
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.discussions.services.toDiscussionCommentRepresentation
import eu.tib.orkg.prototype.statements.application.UserNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.mockk.MockKGateway.VerificationResult.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.Principal
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [DiscussionController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given a Discussion controller")
internal class DiscussionControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var discussionService: DiscussionUseCases

    @MockkBean
    private lateinit var userService: UserService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given a comment is created, when service succeeds, then status is 200 OK and comment is returned`() {
        val topic = ThingId("C1234")
        val userId = UUID.randomUUID()
        val mockPrincipal = mockk<Principal>()
        val user = createUser(id = userId).toUser()
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(userId),
            createdAt = OffsetDateTime.now()
        )
        val createCommand = CreateDiscussionCommentUseCase.CreateCommand(
            topic = topic,
            message = comment.message,
            createdBy = comment.createdBy
        )

        every { mockPrincipal.name } returns userId.toString()
        every { userService.findById(userId) } returns Optional.of(user)
        every { discussionService.create(createCommand) } returns comment.id
        every { discussionService.findByTopicAndCommentId(topic, comment.id) } returns Optional.of(comment.toDiscussionCommentRepresentation())

        val request = mapOf(
            "message" to comment.message
        )

        mockMvc.post("/api/discussions/topic/$topic", mockPrincipal, request)
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/discussions/topic/$topic/${comment.id}"))
            .andExpect(jsonPath("$.id").value(comment.id.value.toString()))
            .andExpect(jsonPath("$.message").value(comment.message))
            .andExpect(jsonPath("$.created_by").value(comment.createdBy.value.toString()))
            .andExpect(jsonPath("$.created_at").value(comment.createdAt.format(ISO_OFFSET_DATE_TIME)))

        verify(exactly = 1) { discussionService.create(createCommand) }
    }

    @Test
    fun `Given a comment is created, when user is unauthorized, then status is 401 UNAUTHORIZED`() {
        val topic = ThingId("C1234")
        val mockPrincipal = mockk<Principal>()

        every { mockPrincipal.name } returns null

        val request = mapOf(
            "message" to "irrelevant"
        )

        mockMvc.post("/api/discussions/topic/$topic", mockPrincipal, request)
            .andExpect(status().isUnauthorized)

        verify(exactly = 0) { discussionService.create(any()) }
    }

    @Test
    fun `Given a comment is created, when user is not found, then status is 400 BAD REQUEST`() {
        val topic = ThingId("C1234")
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()

        every { mockPrincipal.name } returns userId.toString()
        every { userService.findById(userId) } returns Optional.empty()

        val request = mapOf(
            "message" to "irrelevant"
        )

        mockMvc.post("/api/discussions/topic/$topic", mockPrincipal, request)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("""User "$userId" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic"))

        verify(exactly = 0) { discussionService.create(any()) }
    }

    @Test
    fun `Given a comment is created, when service reports topic not found, then status is 404 NOT FOUND`() {
        val topic = ThingId("C1234")
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()
        val user = createUser(id = userId).toUser()

        every { mockPrincipal.name } returns userId.toString()
        every { userService.findById(userId) } returns Optional.of(user)
        every { discussionService.create(any()) } throws TopicNotFound(topic)

        val request = mapOf(
            "message" to "irrelevant"
        )

        mockMvc.post("/api/discussions/topic/$topic", mockPrincipal, request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("""Topic "$topic" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic"))
    }

    @Test
    fun `Given a comment is created, when service reports invalid message contents, then status is 402 FORBIDDEN`() {
        val topic = ThingId("C1234")
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()
        val user = createUser(id = userId).toUser()
        every { mockPrincipal.name } returns userId.toString()
        every { userService.findById(userId) } returns Optional.of(user)
        every { discussionService.create(any()) } throws InvalidContent()

        val request = mapOf(
            "message" to "irrelevant"
        )

        mockMvc.post("/api/discussions/topic/$topic", mockPrincipal, request)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("""Invalid message contents."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic"))
    }

    @Test
    fun `Given a comment is retrieved, when service reports topic not found, then status is 404 NOT FOUND`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { discussionService.findByTopicAndCommentId(topic, id) } throws TopicNotFound(topic)

        mockMvc.perform(get("/api/discussions/topic/$topic/$id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("""Topic "$topic" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic/$id"))
    }

    @Test
    fun `Given a comment is retrieved, when service reports comment not found, then status is 404 NOT FOUND`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { discussionService.findByTopicAndCommentId(topic, id) } returns Optional.empty()

        mockMvc.perform(get("/api/discussions/topic/$topic/$id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("""Comment "$id" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic/$id"))
    }

    @Test
    fun `Given a comment is retrieved, when service reports success, then status is 200 OK and the comment is returned`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = id,
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = OffsetDateTime.now()
        )

        every { discussionService.findByTopicAndCommentId(topic, id) } returns Optional.of(comment.toDiscussionCommentRepresentation())

        mockMvc.perform(get("/api/discussions/topic/$topic/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(comment.id.value.toString()))
            .andExpect(jsonPath("$.message").value(comment.message))
            .andExpect(jsonPath("$.created_by").value(comment.createdBy.value.toString()))
            .andExpect(jsonPath("$.created_at").value(comment.createdAt.format(ISO_OFFSET_DATE_TIME)))
    }

    @Test
    fun `Given several comments about a topic are retrieved, when service reports topic not found, then status is 404 NOT FOUND`() {
        val topic = ThingId("C1234")

        every { discussionService.findAllByTopic(topic, any()) } throws TopicNotFound(topic)

        mockMvc.perform(get("/api/discussions/topic/$topic"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("""Topic "$topic" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic"))
    }

    @Test
    fun `Given several comments about a topic are retrieved, when service reports success, then status is 200 OK and the comments are returned`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = id,
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = OffsetDateTime.now()
        )
        val page = PageImpl(
            listOf(comment.toDiscussionCommentRepresentation()),
            PageRequest.of(0, 5),
            1
        )

        every { discussionService.findAllByTopic(topic, any()) } returns page

        mockMvc.perform(get("/api/discussions/topic/$topic"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].id").value(comment.id.value.toString()))
            .andExpect(jsonPath("$.content[0].message").value(comment.message))
            .andExpect(jsonPath("$.content[0].created_by").value(comment.createdBy.value.toString()))
            .andExpect(jsonPath("$.content[0].created_at").value(comment.createdAt.format(ISO_OFFSET_DATE_TIME)))
    }

    @Test
    fun `Given a comment is being deleted, when service reports success, then status is 204 NO CONTENT`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()

        every { mockPrincipal.name } returns userId.toString()
        every { discussionService.delete(ContributorId(userId), topic, id) } returns Unit

        mockMvc.perform(delete("/api/discussions/topic/$topic/$id").principal(mockPrincipal))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `Given a comment is being deleted, when user is not authorized, then status is 401 UNAUTHORIZED`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val mockPrincipal = mockk<Principal>()

        every { mockPrincipal.name } returns null

        mockMvc.perform(delete("/api/discussions/topic/$topic/$id").principal(mockPrincipal))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Given a comment is being deleted, when service reports user not found, then status is 400 BAD REQUEST`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()

        every { mockPrincipal.name } returns userId.toString()
        every { discussionService.delete(ContributorId(userId), topic, id) } throws UserNotFound(userId)

        mockMvc.perform(delete("/api/discussions/topic/$topic/$id").principal(mockPrincipal))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Given a comment is being deleted, when service reports unauthorized, then status is 401 UNAUTHORIZED`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val mockPrincipal = mockk<Principal>()
        val userId = UUID.randomUUID()

        every { mockPrincipal.name } returns userId.toString()
        every { discussionService.delete(ContributorId(userId), topic, id) } throws Unauthorized()

        mockMvc.perform(delete("/api/discussions/topic/$topic/$id").principal(mockPrincipal))
            .andExpect(status().isUnauthorized)
    }

    private fun MockMvc.post(uriTemplate: String, principal: Principal, body: Map<String, String>) = perform(
        post(uriTemplate)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))
    )
}