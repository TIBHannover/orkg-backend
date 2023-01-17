package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import io.mockk.every
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [StatementController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given a Statement controller")
internal class StatementControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
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

        every { statementService.create(any(), any(), any(), any()) } throws StatementSubjectNotFound(subject)

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Subject "$subject" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))
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

        every { statementService.create(any(), any(), any(), any()) } throws StatementPredicateNotFound(PredicateId(predicate))

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Predicate "$predicate" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))
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

        every { statementService.create(any(), any(), any(), any()) } throws StatementObjectNotFound(`object`)

        mockMvc.perform(performPost(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("""Object "$`object`" not found."""))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements/"))
    }

    private fun performPost(body: Map<String, String>) =
        post("/api/statements/")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))
}
