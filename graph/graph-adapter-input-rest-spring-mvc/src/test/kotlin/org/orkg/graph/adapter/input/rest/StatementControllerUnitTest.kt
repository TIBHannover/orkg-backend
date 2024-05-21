package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
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
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedDeleteRequestTo
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [StatementController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [StatementController::class])
@DisplayName("Given a Statement controller")
@UsesMocking
internal class StatementControllerUnitTest : RestDocsTest("statements") {

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var clock: Clock

    @AfterEach
    fun verifyMockedCalls() {
        confirmVerified(statementService)
        clearAllMocks()
    }

    @Test
    @DisplayName("Given several statements, when filtering by no parameters, then status is 200 OK and statements are returned")
    fun getPaged() {
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createStatement())
        every { flags.isFormattedLabelsEnabled() } returns false

        documentedGetRequestTo("/api/statements")
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    @DisplayName("Given several statements, when they are fetched with all possible filtering parameters, then status is 200 OK and statements are returned")
    fun getPagedWithParameters() {
        val statement = createStatement().copy(
            subject = createResource(classes = setOf(Classes.contribution, ThingId("C123"))),
            `object` = createLiteral()
        )
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(statement)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        val subjectClasses = (statement.subject as Resource).classes
        val subjectId = statement.subject.id
        val subjectLabel = statement.subject.label
        val predicateId = statement.predicate.id
        val createdBy = statement.createdBy
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val objectClasses = setOf(Classes.literal)
        val objectId = statement.`object`.id
        val objectLabel = statement.`object`.label

        documentedGetRequestTo("/api/statements")
            .param("subject_classes", subjectClasses.joinToString(separator = ","))
            .param("subject_id", subjectId.value)
            .param("subject_label", subjectLabel)
            .param("predicate_id", predicateId.value)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("object_classes", objectClasses.joinToString(separator = ","))
            .param("object_id", objectId.value)
            .param("object_label", objectLabel)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("subject_classes").description("A comma-separated set of classes that the subject of the statement must have. The ids `Resource`, `Class` and `Predicate` can be used to filter for a general type of subject. (optional)"),
                        parameterWithName("subject_id").description("Filter for the subject id. (optional)"),
                        parameterWithName("subject_label").description("Filter for the label of the subject. The label has to match exactly. (optional)"),
                        parameterWithName("predicate_id").description("""Filter for the predicate id of the statement. (optional)"""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this statement. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned statement can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned statement can have. (optional)"),
                        parameterWithName("object_classes").description("A comma-separated set of classes that the object of the statement must have. The ids `Resource`, `Class`, `Predicate` and `Literal` can be used to filter for a general type of object. (optional)"),
                        parameterWithName("object_id").description("Filter for the object id. (optional)"),
                        parameterWithName("object_label").description("Filter for the label of the object. The label has to match exactly. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAll(
                pageable = any(),
                subjectClasses = subjectClasses,
                subjectId = subjectId,
                subjectLabel = subjectLabel,
                predicateId = predicateId,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                objectClasses = objectClasses,
                objectId = objectId,
                objectLabel = objectLabel
            )
        }
        verify(exactly = 1) { statementService.countIncomingStatements(any<Set<ThingId>>()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given several statements, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws exception

        mockMvc.perform(get("/api/statements?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/statements"))

        verify(exactly = 1) { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
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
        @DisplayName("when deleting a statement by id and service reports success, then status is 204 NO CONTENT")
        fun deleteById_isNoContent() {
            val id = StatementId("S1")

            every { statementService.delete(id) } just Runs

            documentedDeleteRequestTo("/api/statements/{id}", id)
                .perform()
                .andExpect(status().isNoContent)
                .andDo(
                    documentationHandler.document(
                        pathParameters(
                            parameterWithName("id").description("The identifier of the statement to delete.")
                        )
                    )
                )
                .andDo(generateDefaultDocSnippets())

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

        every {
            statementService.findAll(
                predicateId = statement.predicate.id,
                objectClasses = setOf(Classes.literal),
                objectLabel = statement.`object`.label,
                pageable = any()
            )
        } returns pageOf(statement)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(get("/api/statements/predicate/${statement.predicate.id}/literals?q=${statement.`object`.label}"))
            .andExpect(status().isOk)

        verify(exactly = 1) {
            statementService.findAll(
                predicateId = statement.predicate.id,
                objectClasses = setOf(Classes.literal),
                objectLabel = statement.`object`.label,
                pageable = any()
            )
            statementService.countIncomingStatements(any<Set<ThingId>>())
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
}
