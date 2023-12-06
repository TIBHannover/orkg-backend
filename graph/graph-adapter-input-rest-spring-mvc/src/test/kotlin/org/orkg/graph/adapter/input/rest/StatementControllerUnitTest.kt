package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import java.security.Principal
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementObjectNotFound
import org.orkg.graph.domain.StatementPredicateNotFound
import org.orkg.graph.domain.StatementSubjectNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [StatementController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [StatementController::class])
@DisplayName("Given a Statement controller")
@UsesMocking
internal class StatementControllerUnitTest : RestDocsTest("statements") {

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @AfterEach
    fun verifyMockedCalls() {
        confirmVerified(userRepository, statementService)
        clearAllMocks()
    }

    @Test
    @TestWithMockUser
    fun `Given a statement is created, service reports a missing subject, then status is 404 NOT FOUND`() {
        val subject = "one"
        val predicate = "less_than"
        val `object` = "two"

        val body = mapOf(
            "subject_id" to subject,
            "predicate_id" to predicate,
            "object_id" to `object`
        )

        every { statementService.create(any(), any(), any(), any()) } throws StatementSubjectNotFound(ThingId(subject))

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Subject "$subject" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))

        verify(exactly = 1) {
            statementService.create(
                ContributorId(MockUserId.USER),
                ThingId(subject),
                ThingId(predicate),
                ThingId(`object`),
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a statement is created, service reports a missing predicate, then status is 404 NOT FOUND`() {
        val subject = "one"
        val predicate = "less_than"
        val `object` = "two"

        val body = mapOf(
            "subject_id" to subject,
            "predicate_id" to predicate,
            "object_id" to `object`
        )

        every { statementService.create(any(), any(), any(), any()) } throws StatementPredicateNotFound(
            ThingId(
                predicate
            )
        )

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Predicate "$predicate" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))

        verify(exactly = 1) {
            statementService.create(
                ContributorId(MockUserId.USER),
                ThingId(subject),
                ThingId(predicate),
                ThingId(`object`),
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a statement is created, service reports a missing object, then status is 404 NOT FOUND`() {
        val subject = "one"
        val predicate = "less_than"
        val `object` = "two"

        val body = mapOf(
            "subject_id" to subject,
            "predicate_id" to predicate,
            "object_id" to `object`
        )

        every { statementService.create(any(), any(), any(), any()) } throws StatementObjectNotFound(ThingId(`object`))

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Object "$`object`" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))

        verify(exactly = 1) {
            statementService.create(
                ContributorId(MockUserId.USER),
                ThingId(subject),
                ThingId(predicate),
                ThingId(`object`),
            )
        }
    }

    @Nested
    @DisplayName("Given a user is logged in")
    inner class UserLoggedIn {
        @Test
        @TestWithMockUser
        fun `when deleting a statement by id and service reports success, then status is 204 NO CONTENT`() {
            val id = StatementId("S1")

            every { statementService.delete(id) } just Runs

            delete("/api/statements/{id}", id)
                .perform()
                .andExpect(status().isNoContent)

            verify(exactly = 1) { statementService.delete(id) }
        }
    }

    @Nested
    @Disabled("Spring Security Test does currently not work in unit tests")
    @DisplayName("Given a user is not logged in")
    inner class UserNotLoggedIn {
        @Test
        @WithAnonymousUser
        fun `when trying to delete a statement by id, then status is 403 FORBIDDEN`() {
            val id = StatementId("S1")

            delete("/api/statements/{id}", id)
                .perform()
                .andExpect(status().isForbidden)

            verify(exactly = 0) { statementService.delete(id) }
        }
    }

    @Test
    fun `Given several statements, when searched by predicate id and object literal, then status is 200 OK and statements are returned`() {
        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(),
            `object` = createLiteral(label = "path/to/resource")
        )

        every { statementService.findAllByPredicateAndLabel(any(), any(), any()) } returns pageOf(statement)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(get("/api/statements/predicate/${statement.predicate.id}/literals?q=${statement.`object`.label}"))
            .andExpect(status().isOk)

        verify(exactly = 1) {
            statementService.findAllByPredicateAndLabel(statement.predicate.id, statement.`object`.label, any())
            statementService.countStatementsAboutResources(any())
            flags.isFormattedLabelsEnabled()
        }
    }

    @Test
    fun `Given a thing id, when fetched as a bundle but thing does not exist, then status is 404 NOT FOUND`() {
        val thingId = ThingId("DoesNotExist")
        val exception = ThingNotFound(thingId)

        every {
            statementService.fetchAsBundle(thingId, any(), false, Sort.unsorted())
        } throws exception

        mockMvc.perform(get("/api/statements/$thingId/bundle?includeFirst=false"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/$thingId/bundle"))

        verify(exactly = 1) {
            statementService.fetchAsBundle(thingId, any(), false, Sort.unsorted())
        }
    }

    private fun performPost(body: Map<String, String>) =
        post("/api/statements/")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))

    private fun MockMvc.delete(uriTemplate: String, principal: Principal) =
        perform(delete(uriTemplate).principal(principal))
}
