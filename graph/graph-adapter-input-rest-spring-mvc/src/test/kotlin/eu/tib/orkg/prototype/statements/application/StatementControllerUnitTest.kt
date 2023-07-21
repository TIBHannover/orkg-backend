package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.annotations.UsesMocking
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.verifyAll
import java.net.URLEncoder
import java.security.Principal
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [StatementController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [StatementController::class])
@DisplayName("Given a Statement controller")
@UsesMocking
internal class StatementControllerUnitTest : RestDocsTest("statements") {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var templateRepository: TemplateRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @MockkBean
    private lateinit var principal: Principal

    @AfterEach
    fun verifyMockedCalls() {
        confirmVerified(userRepository, statementService, principal)
        clearAllMocks()
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
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
                ContributorId("f2d66c90-3cbf-4d4f-951f-0fc470f682c4"),
                ThingId(subject),
                ThingId(predicate),
                ThingId(`object`),
            )
        }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
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
                ContributorId("f2d66c90-3cbf-4d4f-951f-0fc470f682c4"),
                ThingId(subject),
                ThingId(predicate),
                ThingId(`object`),
            )
        }
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
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
                ContributorId("f2d66c90-3cbf-4d4f-951f-0fc470f682c4"),
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
        fun `when deleting a statement with a literal object and service reports success, then status is 204 NO CONTENT`() {
            val s = createResource().copy(label = "one")
            val p = createPredicate().copy(label = "has symbol")
            val l = createLiteral().copy(label = "1")
            val st = createStatement(s, p, l)
            val userId = UUID.randomUUID()

            every { principal.name } returns userId.toString()
            every { statementService.delete(st.id!!) } just Runs

            mockMvc.delete("/api/statements/${st.id}", principal)
                .andExpect(status().isNoContent)

            verify(exactly = 1) { statementService.delete(st.id!!) }
            verifyAll { principal.name }
        }

        @Test
        fun `when deleting a statement with a resource object and service reports success, then status is 204 NO CONTENT`() {
            val s = createResource().copy(label = "one")
            val p = createPredicate().copy(label = "has symbol")
            val l = createResource().copy(label = "1")
            val st = createStatement(s, p, l)
            val userId = UUID.randomUUID()

            every { principal.name } returns userId.toString()
            every { statementService.delete(st.id!!) } just Runs

            mockMvc.delete("/api/statements/${st.id}", principal)
                .andExpect(status().isNoContent)

            verify(exactly = 1) { statementService.delete(st.id!!) }
            verifyAll { principal.name }
        }
    }

    @Nested
    @DisplayName("Given a user is not logged in")
    inner class UserNotLoggedIn {
        @Test
        fun `when trying to delete a statement with a literal object, then status is 403 FORBIDDEN`() {
            val s = createResource().copy(label = "one")
            val p = createPredicate().copy(label = "has symbol")
            val l = createLiteral().copy(label = "1")
            val st = createStatement(s, p, l)

            every { principal.name } returns null

            mockMvc.delete("/api/statements/${st.id}", principal)
                .andExpect(status().isForbidden)

            verify(exactly = 0) { statementService.delete(st.id!!) }
            verifyAll { principal.name }
        }

        @Test
        fun `when trying to delete a statement with a resource object, then status is 403 FORBIDDEN`() {
            val s = createResource().copy(label = "one")
            val p = createPredicate().copy(label = "has symbol")
            val l = createResource().copy(label = "1")
            val st = createStatement(s, p, l)

            every { principal.name } returns null

            mockMvc.delete("/api/statements/${st.id}", principal)
                .andExpect(status().isForbidden)

            verify(exactly = 0) { statementService.delete(st.id!!) }
            verifyAll { principal.name }
        }
    }

    @Test
    fun `Given several statements, when searched by predicate id and object literal, then status is 200 OK and statements are returned`() {
        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(),
            `object` = createLiteral(
                value = "path/to/resource"
            )
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

    private fun performPost(body: Map<String, String>) =
        post("/api/statements/")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))

    private fun MockMvc.delete(uriTemplate: String, principal: Principal) =
        perform(delete(uriTemplate).principal(principal))
}
