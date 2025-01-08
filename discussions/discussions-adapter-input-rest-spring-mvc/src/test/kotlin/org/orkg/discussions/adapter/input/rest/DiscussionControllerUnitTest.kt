package org.orkg.discussions.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.discussions.adapter.input.rest.json.DiscussionsJacksonModule
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId
import org.orkg.discussions.domain.InvalidContent
import org.orkg.discussions.domain.TopicNotFound
import org.orkg.discussions.input.CreateDiscussionCommentUseCase
import org.orkg.discussions.input.DiscussionUseCases
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.UserNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(classes = [DiscussionController::class, ExceptionHandler::class, CommonJacksonModule::class, DiscussionsJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [DiscussionController::class])
internal class DiscussionControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var discussionService: DiscussionUseCases

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is created, when service succeeds, then status is 200 OK and comment is returned`() {
        val topic = ThingId("C1234")
        val contributor = createContributor(id = ContributorId(MockUserId.USER))
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = contributor.id,
            createdAt = OffsetDateTime.now(clock)
        )
        val createCommand = CreateDiscussionCommentUseCase.CreateCommand(
            topic = topic,
            message = comment.message,
            createdBy = comment.createdBy
        )

        every { discussionService.create(createCommand) } returns comment.id
        every { discussionService.findByTopicAndCommentId(topic, comment.id) } returns Optional.of(comment)

        val request = mapOf(
            "message" to comment.message
        )

        post("/api/discussions/topic/$topic")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/discussions/topic/$topic/${comment.id}"))
            .andExpect(jsonPath("$.id").value(comment.id.value.toString()))
            .andExpect(jsonPath("$.message").value(comment.message))
            .andExpect(jsonPath("$.created_by").value(comment.createdBy.value.toString()))
            .andExpect(jsonPath("$.created_at").value(comment.createdAt.format(ISO_OFFSET_DATE_TIME)))

        verify(exactly = 1) { discussionService.create(createCommand) }
    }

    @Test
    fun `Given a comment is created, when user is not logged in, then status is 403 FORBIDDEN`() {
        val topic = ThingId("C1234")

        val request = mapOf(
            "message" to "irrelevant"
        )

        post("/api/discussions/topic/$topic")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isForbidden)

        verify(exactly = 0) { discussionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is created, when service reports topic not found, then status is 404 NOT FOUND`() {
        val topic = ThingId("C1234")

        every { discussionService.create(any()) } throws TopicNotFound(topic)

        val request = mapOf(
            "message" to "irrelevant"
        )

        post("/api/discussions/topic/$topic")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("""Topic "$topic" not found."""))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/discussions/topic/$topic"))
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is created, when service reports invalid message contents, then status is 403 FORBIDDEN`() {
        val topic = ThingId("C1234")
        val request = mapOf(
            "message" to "irrelevant"
        )

        every { discussionService.create(any()) } throws InvalidContent()

        post("/api/discussions/topic/$topic")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
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

        mockMvc.perform(get("/api/discussions/topic/{topic}/{id}", topic, id))
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

        mockMvc.perform(get("/api/discussions/topic/{topic}/{id}", topic, id))
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
            createdAt = OffsetDateTime.now(clock)
        )

        every { discussionService.findByTopicAndCommentId(topic, id) } returns Optional.of(comment)

        mockMvc.perform(get("/api/discussions/topic/{topic}/{id}", topic, id))
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

        mockMvc.perform(get("/api/discussions/topic/{topic}", topic))
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
            createdAt = OffsetDateTime.now(clock)
        )
        val page = PageImpl(
            listOf(comment),
            PageRequest.of(0, 5),
            1
        )

        every { discussionService.findAllByTopic(topic, any()) } returns page

        mockMvc.perform(get("/api/discussions/topic/{topic}", topic))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].id").value(comment.id.value.toString()))
            .andExpect(jsonPath("$.content[0].message").value(comment.message))
            .andExpect(jsonPath("$.content[0].created_by").value(comment.createdBy.value.toString()))
            .andExpect(jsonPath("$.content[0].created_at").value(comment.createdAt.format(ISO_OFFSET_DATE_TIME)))
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is being deleted, when service reports success, then status is 204 NO CONTENT`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { discussionService.delete(ContributorId(MockUserId.USER), topic, id) } returns Unit

        mockMvc.perform(delete("/api/discussions/topic/{topic}/{id}", topic, id))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `Given a comment is being deleted, when user is not logged in, then status is 403 FORBIDDEN`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())

        mockMvc.perform(delete("/api/discussions/topic/{topic}/{id}", topic, id))
            .andExpect(status().isForbidden)
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is being deleted, when service reports user not found, then status is 400 BAD REQUEST`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { discussionService.delete(ContributorId(MockUserId.USER), topic, id) } throws UserNotFound(MockUserId.USER)

        mockMvc.perform(delete("/api/discussions/topic/{topic}/{id}", topic, id))
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockUser
    fun `Given a comment is being deleted, when service reports forbidden, then status is 403 FORBIDDEN`() {
        val topic = ThingId("C1234")
        val id = DiscussionCommentId(UUID.randomUUID())
        val contributorId = ContributorId(MockUserId.USER)

        every { discussionService.delete(contributorId, topic, id) } throws NeitherOwnerNorCurator(contributorId)

        mockMvc.perform(delete("/api/discussions/topic/{topic}/{id}", topic, id))
            .andExpect(status().isForbidden)
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
