package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.adapter.input.rest.StatementController.CreateStatementRequest
import org.orkg.graph.adapter.input.rest.StatementController.UpdateStatementRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.statementResponseFields
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementAlreadyExists
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementInUse
import org.orkg.graph.domain.StatementNotFound
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.graph.domain.StatementObjectNotFound
import org.orkg.graph.domain.StatementPredicateNotFound
import org.orkg.graph.domain.StatementSubjectNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectBundle
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.repeatable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(classes = [StatementController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [StatementController::class])
internal class StatementControllerUnitTest : MockMvcBaseTest("statements") {
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @DisplayName("Given a statement, when it is fetched by id and service succeeds, then status is 200 OK and statement is returned")
    fun findById() {
        val statement = createStatement()
        every { statementService.findById(statement.id) } returns Optional.of(statement)
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/{id}", statement.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectStatement()
            .andDocument {
                summary("Fetching statements")
                description(
                    """
                    A `GET` request provides information about a statement.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the statement to retrieve.")
                )
                responseFields<StatementRepresentation>(statementResponseFields())
                throws(StatementNotFound::class)
            }

        verify(exactly = 1) { statementService.findById(statement.id) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
    }

    @Test
    @DisplayName("Given several statements, when filtering by no parameters, then status is 200 OK and statements are returned")
    fun getPaged() {
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createStatement())
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    @DisplayName("Given several statements, when they are fetched with all possible filtering parameters, then status is 200 OK and statements are returned")
    fun findAll() {
        val statement = createStatement().copy(
            subject = createResource(classes = setOf(Classes.contribution, ThingId("C123"))),
            `object` = createLiteral()
        )
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(statement)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

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
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDocument {
                summary("Listing statements")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<statements-fetch,statements>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("subject_classes").description("A comma-separated set of classes that the subject of the statement must have. The ids `Resource`, `Class` and `Predicate` can be used to filter for a general type of subject. (optional)").repeatable().optional(),
                    parameterWithName("subject_id").description("Filter for the subject id. (optional)").optional(),
                    parameterWithName("subject_label").description("Filter for the label of the subject. The label has to match exactly. (optional)").optional(),
                    parameterWithName("predicate_id").description("""Filter for the predicate id of the statement. (optional)""").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this statement. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned statement can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned statement can have. (optional)").optional(),
                    parameterWithName("object_classes").description("A comma-separated set of classes that the object of the statement must have. The ids `Resource`, `Class`, `Predicate` and `Literal` can be used to filter for a general type of object. (optional)").repeatable().optional(),
                    parameterWithName("object_id").description("Filter for the object id. (optional)").optional(),
                    parameterWithName("object_label").description("Filter for the label of the object. The label has to match exactly. (optional)").optional(),
                )
                pagedResponseFields<StatementRepresentation>(statementResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
            statementService.countAllIncomingStatementsById(any<Set<ThingId>>())
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    fun `Given several statements, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws exception

        get("/api/statements")
            .param("sort", "unknown")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a statement is created, when service succeeds, then status is 201 CREATED")
    fun create() {
        val id = StatementId("S123")
        val statement = createStatement().copy(id = id, subject = createResource(), `object` = createLiteral())
        val request = CreateStatementRequest(
            id = id,
            subjectId = statement.subject.id,
            predicateId = statement.predicate.id,
            objectId = statement.`object`.id
        )
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            subjectId = request.subjectId,
            predicateId = request.predicateId,
            objectId = request.objectId
        )

        every { statementService.create(command) } returns id

        documentedPostRequestTo("/api/statements")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/statements/$id")))
            .andDocument {
                summary("Creating statements")
                description(
                    """
                    A `POST` request creates a new statement.
                    The response will be `201 Created` when successful.
                    The statement can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. If the subject, predicate or object cannot be found, the return status will be `400 BAD REQUEST`.
                    2. If the subject is a rosetta stone statement, the return status will be `400 BAD REQUEST`.
                    3. If the statement represents a list element statement, the return status will be `400 BAD REQUEST`.
                    4. If the subject is a literal, the return status will be `400 BAD REQUEST`.
                    5. If a statement id is provided and a statement with that id already exists, the return status will be `400 BAD REQUEST`.
                    6. If a statement with the specified subject, predicate and object already exists, the id of the existing statement will be returned and the status will be `201 CREATED`.
                    ====
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created statement can be fetched from.")
                )
                requestFields<CreateStatementRequest>(
                    fieldWithPath("id").description("The statement id. (optional)").optional(),
                    fieldWithPath("subject_id").description("The id of the subject of the statement."),
                    fieldWithPath("predicate_id").description("The id of the predicate of the statement."),
                    fieldWithPath("object_id").description("The id of the object of the statement.")
                )
                throws(
                    StatementSubjectNotFound::class,
                    InvalidStatement::class,
                    StatementPredicateNotFound::class,
                    StatementObjectNotFound::class,
                    StatementAlreadyExists::class,
                )
            }

        verify(exactly = 1) { statementService.create(command) }
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
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            subjectId = ThingId(subject),
            predicateId = ThingId(predicate),
            objectId = ThingId(`object`)
        )

        every { statementService.create(command) } throws StatementSubjectNotFound(ThingId(subject))

        post("/api/statements")
            .content(body)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_subject_not_found")

        verify(exactly = 1) { statementService.create(command) }
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
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            subjectId = ThingId(subject),
            predicateId = ThingId(predicate),
            objectId = ThingId(`object`)
        )

        every { statementService.create(command) } throws StatementPredicateNotFound(ThingId(predicate))

        post("/api/statements")
            .content(body)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_predicate_not_found")

        verify(exactly = 1) { statementService.create(command) }
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
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            subjectId = ThingId(subject),
            predicateId = ThingId(predicate),
            objectId = ThingId(`object`)
        )

        every { statementService.create(command) } throws StatementObjectNotFound(ThingId(`object`))

        post("/api/statements")
            .content(body)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_object_not_found")

        verify(exactly = 1) { statementService.create(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a statement is updated, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = StatementId("S1")
        val subjectId = ThingId("R123")
        val predicateId = ThingId("P123")
        val objectId = ThingId("C123")
        val request = UpdateStatementRequest(
            subjectId = subjectId,
            predicateId = predicateId,
            objectId = objectId,
        )
        every { statementService.update(any()) } just runs

        documentedPutRequestTo("/api/statements/{id}", id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/statements/$id")))
            .andDocument {
                summary("Updating statements")
                description(
                    """
                    A `PUT` request updates an existing statement with the given parameters.
                    The response will be `204 NO CONTENT` when successful.
                    The updated statement can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of statement.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated statement can be fetched from.")
                )
                requestFields<UpdateStatementRequest>(
                    fieldWithPath("subject_id").description("The updated id of the subject entity of the statement. (optional)").optional(),
                    fieldWithPath("predicate_id").description("The updated id of the predicate of the statement. (optional)").optional(),
                    fieldWithPath("object_id").description("The updated id of the object entity of the statement. (optional)").optional(),
                )
                throws(
                    StatementNotFound::class,
                    StatementNotModifiable::class,
                    InvalidStatement::class,
                    StatementSubjectNotFound::class,
                    StatementPredicateNotFound::class,
                    StatementObjectNotFound::class,
                )
            }

        verify(exactly = 1) {
            statementService.update(
                withArg {
                    it.statementId shouldBe id
                    it.subjectId shouldBe subjectId
                    it.predicateId shouldBe predicateId
                    it.objectId shouldBe objectId
                }
            )
        }
    }

    @Test
    @DisplayName("Given a statement, when it is fetched by id and service succeeds, then status is 200 OK and statement is returned")
    fun fetchAsBundle() {
        val id = ThingId("R1")
        val bundleConfiguration = BundleConfiguration(
            minLevel = 1,
            maxLevel = 5,
            blacklist = listOf(Classes.researchField),
            whitelist = listOf()
        )
        val includeFirst = true

        every {
            statementService.fetchAsBundle(
                thingId = id,
                configuration = bundleConfiguration,
                includeFirst = any(),
                sort = any()
            )
        } returns Bundle(id, mutableListOf(createStatement()))
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/{id}/bundle", id)
            .param("min_level", bundleConfiguration.minLevel.toString())
            .param("max_level", bundleConfiguration.maxLevel.toString())
            .param("blacklist", bundleConfiguration.blacklist.joinToString(","))
            .param("whitelist", bundleConfiguration.whitelist.joinToString(","))
            .param("include_first", includeFirst.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectBundle()
            .andDocument {
                summary("Fetching statements as bundles")
                description(
                    """
                    A `Bundle` is a collection of statements that represent the subgraph starting from a specified entity in the graph.
                    A `GET` request fetches a subgraph of a certain entity and returns all the statements as a bundle.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the root of the bundle.")
                )
                queryParameters(
                    parameterWithName("min_level").description("The minimum hops a statement must be away from the root entity, in order to be included in the bundle. (optional)").optional(),
                    parameterWithName("max_level").description("The maximum hops a statement can be away from the root entity, in order to be included in the bundle. (optional)").optional(),
                    parameterWithName("blacklist").description("A set of class ids that will prevent subgraph expansion. No resource in the resulting bundle will be an instance of the provided classes. (optional)").optional(),
                    parameterWithName("whitelist").description("A set of class ids that are exclusively used for subgraph expansion. All resources in the resulting bundle are an instance of one of the provided classes. If not provided, all resources will be considered for subgraph expansion. (optional)").optional(),
                    parameterWithName("include_first").description("Whether to additionally include first level statements, regardless of other filters. (optional, default: true)").optional(),
                )
                responseFields<BundleRepresentation>(
                    fieldWithPath("root").description("The root ID of the object."),
                    subsectionWithPath("statements[]").description("The list of statements.")
                )
                throws(ThingNotFound::class)
            }

        verify(exactly = 1) { statementService.fetchAsBundle(id, bundleConfiguration, any(), any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
    }

    @Nested
    @DisplayName("Given a user is logged in")
    inner class UserLoggedIn {
        @Test
        @TestWithMockUser
        @DisplayName("when deleting a statement by id and service reports success, then status is 204 NO CONTENT")
        fun deleteById() {
            val id = StatementId("S1")

            every { statementService.deleteById(id) } just Runs

            documentedDeleteRequestTo("/api/statements/{id}", id)
                .perform()
                .andExpect(status().isNoContent)
                .andDocument {
                    summary("Deleting statements")
                    description(
                        """
                        A `DELETE` request deletes a statement from the graph.
                        It does not delete its subject or object, except for literal objects.
                        The response will be `204 No Content` when successful.
                        
                        [NOTE]
                        ====
                        1. If the statement is not modifiable, the return status will be `403 FORBIDDEN`.
                        2. If the statement is a <<lists,list>> element statement, the return status will be `403 FORBIDDEN`.
                        ====
                        """
                    )
                    pathParameters(
                        parameterWithName("id").description("The identifier of the statement to delete.")
                    )
                    throws(StatementNotModifiable::class, StatementInUse::class)
                }

            verify(exactly = 1) { statementService.deleteById(id) }
        }
    }

    @Nested
    @DisplayName("Given a user is not logged in")
    inner class UserNotLoggedIn {
        @Test
        fun `when trying to delete a statement by id, then status is 403 FORBIDDEN`() {
            val id = StatementId("S1")

            delete("/api/statements/{id}", id)
                .perform()
                .andExpectErrorStatus(FORBIDDEN)
                .andExpectType("orkg:problem:access_denied")

            verify(exactly = 0) { statementService.deleteById(id) }
        }
    }

    @Test
    fun `Given a thing id, when fetched as a bundle but thing does not exist, then status is 404 NOT FOUND`() {
        val id = ThingId("DoesNotExist")
        val exception = ThingNotFound(id)

        every {
            statementService.fetchAsBundle(id, any(), false, Sort.unsorted())
        } throws exception

        get("/api/statements/{id}/bundle", id)
            .param("includeFirst", "false")
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:thing_not_found")

        verify(exactly = 1) {
            statementService.fetchAsBundle(id, any(), false, Sort.unsorted())
        }
    }
}
