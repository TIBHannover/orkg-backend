package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneStatementController.CreateRosettaStoneStatementRequest
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneStatementController.UpdateRosettaStoneStatementRequest
import org.orkg.contenttypes.domain.CannotDeleteIndividualRosettaStoneStatementVersion
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.MissingObjectPositionValue
import org.orkg.contenttypes.domain.MissingSubjectPositionValue
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectPositionValueDoesNotMatchPattern
import org.orkg.contenttypes.domain.ObjectPositionValueTooHigh
import org.orkg.contenttypes.domain.ObjectPositionValueTooLow
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.RosettaStoneStatementInUse
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.TooManyObjectPositionValues
import org.orkg.contenttypes.domain.TooManySubjectPositionValues
import org.orkg.contenttypes.domain.testing.asciidoc.allowedCertaintyValues
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateClassRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateListRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateLiteralRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreatePredicateRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateResourceRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.rosettaStoneStatementResponseFields
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRosettaStoneStatement
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(classes = [RosettaStoneStatementController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [RosettaStoneStatementController::class])
internal class RosettaStoneStatementControllerUnitTest : MockMvcBaseTest("rosetta-stone-statements") {
    @MockkBean
    private lateinit var statementService: RosettaStoneStatementUseCases

    @Test
    @DisplayName("Given a statement, when it is fetched by id and service succeeds, then status is 200 OK and statement is returned")
    fun findById() {
        val statement = createRosettaStoneStatement()
        every { statementService.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        documentedGetRequestTo("/api/rosetta-stone/statements/{id}", statement.id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneStatement()
            .andDocument {
                summary("Fetching rosetta stone statements")
                description(
                    """
                    A `GET` request provides information about a rosetta stone statement or a specific rosetta stone statement version.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the rosetta stone statement to retrieve.")
                )
                responseFields<RosettaStoneStatementRepresentation>(rosettaStoneStatementResponseFields())
                throws(RosettaStoneStatementNotFound::class, RosettaStoneStatementVersionNotFound::class)
            }

        verify(exactly = 1) { statementService.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when it is fetched by id and service reports missing rosetta stone statement, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { statementService.findByIdOrVersionId(id) } returns Optional.empty()

        get("/api/rosetta-stone/statements/{id}", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:rosetta_stone_statement_not_found")

        verify(exactly = 1) { statementService.findByIdOrVersionId(id) }
    }

    @Test
    @DisplayName("Given several rosetta stone statements, when they are fetched, then status is 200 OK and rosetta stone statements are returned")
    fun getPaged() {
        val statement = createRosettaStoneStatement()
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(statement)

        documentedGetRequestTo("/api/rosetta-stone/statements")
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaStoneStatement("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("Given several rosetta stone statements, when filtering by several parameters, then status is 200 OK and rosetta stone statements are returned")
    fun findAll() {
        val statement = createRosettaStoneStatement()
        every { statementService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(statement)

        val context = ThingId("R123")
        val templateId = ThingId("R456")
        val templateTargetClassId = ThingId("C123")
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")

        documentedGetRequestTo("/api/rosetta-stone/statements")
            .param("context", context.value)
            .param("template_id", templateId.value)
            .param("class_id", templateTargetClassId.value)
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaStoneStatement("$.content[*]")
            .andDocument {
                summary("Listing rosetta stone statements")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<rosetta-stone-statements-fetch,rosetta stone statements>>.
                    If no paging request parameters are provided, the default values will be used.
                    
                    NOTE: Only the most recent versions of rosetta stone statements will be returned.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("context").description("Filter for the id of the context that the rosetta stone statement was created with. (optional)").optional(),
                    parameterWithName("template_id").description("Filter for the template id that was used to instantiate the rosetta stone statement. (optional)").optional(),
                    parameterWithName("class_id").description("Filter for the class id of the rosetta stone statement. (optional)").optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created the first version of the rosetta stone statement. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned rosetta stone statement can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned rosetta stone statement can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the rosetta stone statement belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the rosetta stone statement belongs to. (optional)").format("uuid").optional(),
                )
                pagedResponseFields<RosettaStoneStatementRepresentation>(rosettaStoneStatementResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            statementService.findAll(
                pageable = any(),
                context = context,
                templateId = templateId,
                templateTargetClassId = templateTargetClassId,
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        }
    }

    @Test
    @DisplayName("Given a rosetta stone statement, when fetching all of its versions, then status is 200 OK and rosetta stone statements are returned")
    fun findAllVersionsById() {
        val statement = createRosettaStoneStatement()
        every { statementService.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        documentedGetRequestTo("/api/rosetta-stone/statements/{id}/versions", statement.id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneStatement("$[*]")
            .andDocument {
                summary("Listing rosetta stone statement versions")
                description(
                    """
                    A `GET` request returns a list of <<rosetta-stone-statements-fetch,rosetta stone statement>> versions.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the rosetta stone statement version.")
                )
                listResponseFields<RosettaStoneStatementRepresentation>(rosettaStoneStatementResponseFields())
                throws(RosettaStoneStatementNotFound::class)
            }

        verify(exactly = 1) { statementService.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when fetching all of its versions and service reports missing rosetta stone statement, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { statementService.findByIdOrVersionId(id) } returns Optional.empty()

        get("/api/rosetta-stone/statements/{id}/versions", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:rosetta_stone_statement_not_found")

        verify(exactly = 1) { statementService.findByIdOrVersionId(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone statement create request, when service succeeds, it creates and returns the statement")
    fun create() {
        val id = ThingId("R123")
        every { statementService.create(any()) } returns id

        documentedPostRequestTo("/api/rosetta-stone/statements")
            .content(createRosettaStoneStatementRequest())
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/rosetta-stone/statements/$id")))
            .andDocument {
                summary("Creating rosetta stone statements")
                description(
                    """
                    A `POST` request creates a new rosetta stone statement with all the given parameters.
                    The response will be `201 Created` when successful.
                    The rosetta stone statement can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created rosetta stone statement can be fetched from."),
                )
                requestFields<CreateRosettaStoneStatementRequest>(
                    fieldWithPath("template_id").description("The identifier of the templates that was used to instantiate the rosetta stone statement."),
                    fieldWithPath("context").description("The ID of the context resource of the rosetta stone statement, possibly indicating the origin of a statement. (optional)").optional(),
                    fieldWithPath("subjects[]").description("The ordered list of subject instance IDs used in the rosetta stone statement."),
                    fieldWithPath("objects[]").description("The ordered list of object position instances used in the rosetta stone statement. The order of the objects corresponds to the order of the properties of the rosetta stone template."),
                    fieldWithPath("objects[][]").description("The ordered list of object instance IDs used for the object position index defined by the outer array."),
                    fieldWithPath("certainty").description("""The certainty of the rosetta stone statement. Either of $allowedCertaintyValues."""),
                    *mapOfCreateResourceRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateLiteralRequestPartRequestFields().toTypedArray(),
                    *mapOfCreatePredicateRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateListRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateClassRequestPartRequestFields().toTypedArray(),
                    fieldWithPath("negated").description("Whether the statement represented by the rosetta stone statement instance is semantically negated. (optional, default: false)").optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
                    fieldWithPath("extraction_method").description("""The method used to extract the rosetta stone statement. Either of $allowedExtractionMethodValues."""),
                )
                throws(
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    RosettaStoneTemplateNotFound::class,
                    ResourceNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLabel::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    MissingInputPositions::class,
                    TooManyInputPositions::class,
                    MissingSubjectPositionValue::class,
                    MissingObjectPositionValue::class,
                    TooManySubjectPositionValues::class,
                    TooManyObjectPositionValues::class,
                    LabelDoesNotMatchPattern::class,
                    NumberTooHigh::class,
                    NumberTooLow::class,
                    ObjectPositionValueDoesNotMatchPattern::class,
                    ObjectPositionValueTooLow::class,
                    ObjectPositionValueTooHigh::class,
                    RosettaStoneStatementNotFound::class,
                    RosettaStoneStatementVersionNotFound::class,
                    NestedRosettaStoneStatement::class,
                )
            }

        verify(exactly = 1) { statementService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone statement update request, when service succeeds, it updates the statement")
    fun update() {
        val id = ThingId("R123")
        val newId = ThingId("R124")
        every { statementService.update(any()) } returns newId

        documentedPostRequestTo("/api/rosetta-stone/statements/{id}", id)
            .content(updateRosettaStoneStatementRequest())
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/rosetta-stone/statements/$newId")))
            .andDocument {
                summary("Updataing rosetta stone statements")
                description(
                    """
                    A `POST` request creates a new version of an existing rosetta stone statement with all the given parameters.
                    The response will be `201 Created` when successful.
                    The revised rosetta stone statement (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the rosetta stone statement version.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created rosetta stone statement can be fetched from.")
                )
                requestFields<UpdateRosettaStoneStatementRequest>(
                    fieldWithPath("subjects[]").description("The ordered list of subject instance IDs used in the updated rosetta stone statement."),
                    fieldWithPath("objects[]").description("The ordered list of object position instances used in the updated rosetta stone statement. The order of the objects corresponds to the order of the properties of the rosetta stone template."),
                    fieldWithPath("objects[][]").description("The ordered list of object instance IDs used for the object position index defined by the outer array."),
                    fieldWithPath("certainty").description("""The certainty of the updated rosetta stone statement. Either of $allowedCertaintyValues."""),
                    *mapOfCreateResourceRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateLiteralRequestPartRequestFields().toTypedArray(),
                    *mapOfCreatePredicateRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateListRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateClassRequestPartRequestFields().toTypedArray(),
                    fieldWithPath("negated").description("Whether the statement represented by the updated rosetta stone statement instance is semantically negated. (optional, default: false)").optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
                    fieldWithPath("extraction_method").description("""The method used to extract the updated rosetta stone statement. Either of $allowedExtractionMethodValues."""),
                )
                throws(
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    RosettaStoneStatementNotModifiable::class,
                    RosettaStoneTemplateNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLabel::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    MissingInputPositions::class,
                    TooManyInputPositions::class,
                    MissingSubjectPositionValue::class,
                    MissingObjectPositionValue::class,
                    TooManySubjectPositionValues::class,
                    TooManyObjectPositionValues::class,
                    LabelDoesNotMatchPattern::class,
                    NumberTooHigh::class,
                    NumberTooLow::class,
                    ObjectPositionValueDoesNotMatchPattern::class,
                    ObjectPositionValueTooLow::class,
                    ObjectPositionValueTooHigh::class,
                    RosettaStoneStatementNotFound::class,
                    RosettaStoneStatementVersionNotFound::class,
                    NestedRosettaStoneStatement::class,
                )
            }

        verify(exactly = 1) { statementService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone statement, when soft deleting and service succeeds, then status is 204 NO CONTENT")
    fun softDeleteById() {
        val id = ThingId("R123")
        every { statementService.softDeleteById(id, any()) } just runs

        documentedDeleteRequestTo("/api/rosetta-stone/statements/{id}", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Soft-deleting rosetta stone statements")
                description(
                    """
                    A `DELETE` request soft-deletes a rosetta stone statement with all its versions.
                    The response will be `204 No Content` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the rosetta stone statement to soft-delete.")
                )
                throws(
                    RosettaStoneStatementNotModifiable::class,
                    CannotDeleteIndividualRosettaStoneStatementVersion::class,
                )
            }

        verify(exactly = 1) { statementService.softDeleteById(id, ContributorId(MockUserId.USER)) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given a rosetta stone statement, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun deleteById() {
        val id = ThingId("R123")
        every { statementService.deleteById(id, any()) } just runs

        documentedDeleteRequestTo("/api/rosetta-stone/statements/{id}/versions", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Fully deleting rosetta stone statements")
                description(
                    """
                    A `DELETE` request fully deletes a rosetta stone statement with all its versions.
                    The response will be `204 No Content` when successful.
                    
                    NOTE: The user performing the action needs to be a curator.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the rosetta stone statement to delete.")
                )
                throws(
                    RosettaStoneStatementNotModifiable::class,
                    CannotDeleteIndividualRosettaStoneStatementVersion::class,
                    ContributorNotFound::class,
                    NotACurator::class,
                    RosettaStoneStatementInUse::class,
                )
            }

        verify(exactly = 1) { statementService.deleteById(id, ContributorId(MockUserId.CURATOR)) }
    }

    private fun createRosettaStoneStatementRequest() =
        CreateRosettaStoneStatementRequest(
            templateId = ThingId("R456"),
            context = ThingId("R789"),
            subjects = listOf("R258", "R369", "#temp1"),
            objects = listOf(
                listOf("R987", "R654", "#temp2", "#temp3"),
                listOf("R321", "R741", "#temp4", "#temp5")
            ),
            certainty = Certainty.HIGH,
            negated = false,
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralRequestPart("0.1", Literals.XSD.DECIMAL.prefixedUri)
            ),
            predicates = mapOf(
                "#temp3" to CreatePredicateRequestPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to CreateListRequestPart(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to CreateClassRequestPart(
                    label = "class",
                    uri = ParsedIRI.create("https://orkg.org/class/C1")
                )
            ),
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )

    private fun updateRosettaStoneStatementRequest() =
        UpdateRosettaStoneStatementRequest(
            subjects = listOf("R258", "R369", "#temp1"),
            objects = listOf(
                listOf("R987", "R654", "#temp2", "#temp3"),
                listOf("R321", "R741", "#temp4", "#temp5")
            ),
            certainty = Certainty.HIGH,
            negated = false,
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralRequestPart("0.1", Literals.XSD.DECIMAL.prefixedUri)
            ),
            predicates = mapOf(
                "#temp3" to CreatePredicateRequestPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to CreateListRequestPart(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to CreateClassRequestPart(
                    label = "class",
                    uri = ParsedIRI.create("https://orkg.org/class/C1")
                )
            ),
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )
}
