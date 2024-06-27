package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRosettaStoneStatement
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedDeleteRequestTo
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        RosettaStoneStatementController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [RosettaStoneStatementController::class])
@DisplayName("Given a Rosetta Stone Statement controller")
internal class RosettaStoneStatementControllerUnitTest : RestDocsTest("rosetta-stone-statements") {

    @MockkBean
    private lateinit var statementService: RosettaStoneStatementUseCases

    @Test
    @DisplayName("Given a statement, when it is fetched by id and service succeeds, then status is 200 OK and statement is returned")
    fun getSingle() {
        val statement = createDummyRosettaStoneStatement()
        every { statementService.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        documentedGetRequestTo("/api/rosetta-stone/statements/{id}", statement.id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneStatement()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the rosetta stone statement to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the rosetta stone statement."),
                        fieldWithPath("context").description("The ID of the context resource of the rosetta stone statement, possibly indicating the origin of a statement. (optional)"),
                        fieldWithPath("template_id").description("The identifier of the template that was used to instantiate the rosetta stone statement."),
                        fieldWithPath("class_id").description("The identifier of the class of the rosetta stone statement. This class is equivalent to the target class of the template used to instantiate the rosetta stone statement."),
                        fieldWithPath("version_id").description("The ID of the backing version of the rosetta stone statement contents."),
                        fieldWithPath("latest_version_id").description("The ID of the rosetta stone statement that always points to the latest version of this statement."),
                        fieldWithPath("formatted_label").description("The formatted label at the time of creation of the template used to instantiate the rosetta stone statement."),
                        subsectionWithPath("subjects[]").description("The ordered list of subject instance references used in the rosetta stone statement."),
                        fieldWithPath("objects[]").description("The ordered list of object position instances used in the rosetta stone statement."),
                        subsectionWithPath("objects[][]").description("The ordered list of object instance references used for the object position index defined by the outer array."),
                        timestampFieldWithPath("created_at", "the rosetta stone statement was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this rosetta stone statement."),
                        fieldWithPath("certainty").description("""The certainty of the rosetta stone statement. Either of ${Certainty.entries.joinToString { "\"$it\"" }}."""),
                        fieldWithPath("negated").description("Whether the statement represented by the rosetta stone statement instance is semantically negated."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the rosetta stone statement. Either of ${ExtractionMethod.entries.joinToString { "\"$it\"" }}."""),
                        fieldWithPath("visibility").description("""Visibility of the rosetta stone statement. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this rosetta stone statement.").optional(),
                        fieldWithPath("modifiable").description("Whether this rosetta stone statement can be modified."),
                        timestampFieldWithPath("deleted_at", "the rosetta stone statement was deleted").optional(),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("deleted_by").type("String").description("The UUID of the user or service who deleted this rosetta stone statement.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when it is fetched by id and service reports missing rosetta stone statement, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = RosettaStoneStatementNotFound(id)
        every { statementService.findByIdOrVersionId(id) } returns Optional.empty()

        get("/api/rosetta-stone/statements/$id")
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/rosetta-stone/statements/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { statementService.findByIdOrVersionId(id) }
    }

    @Test
    @DisplayName("Given several rosetta stone statements, when they are fetched, then status is 200 OK and rosetta stone statements are returned")
    fun getPaged() {
        val statement = createDummyRosettaStoneStatement()
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
    fun getPagedWithParameters() {
        val statement = createDummyRosettaStoneStatement()
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
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("context").description("Filter for the id of the context that the rosetta stone statement was created with. (optional)"),
                        parameterWithName("template_id").description("Filter for the template id that was used to instantiate the rosetta stone statement. (optional)"),
                        parameterWithName("class_id").description("Filter for the class id of the rosetta stone statement. (optional)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created the first version of the rosetta stone statement. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned rosetta stone statement can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned rosetta stone statement can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the rosetta stone statement belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the rosetta stone statement belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun getAllVersions() {
        val statement = createDummyRosettaStoneStatement()
        every { statementService.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        documentedGetRequestTo("/api/rosetta-stone/statements/{id}/versions", statement.id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneStatement("$[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when fetching all of its versions and service reports missing rosetta stone statement, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = RosettaStoneStatementNotFound(id)
        every { statementService.findByIdOrVersionId(id) } returns Optional.empty()

        get("/api/rosetta-stone/statements/$id/versions")
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/rosetta-stone/statements/$id/versions"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created rosetta stone statement can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("template_id").description("The identifier of the templates that was used to instantiate the rosetta stone statement."),
                        fieldWithPath("context").description("The ID of the context resource of the rosetta stone statement, possibly indicating the origin of a statement. (optional)").optional(),
                        fieldWithPath("subjects[]").description("The ordered list of subject instance IDs used in the rosetta stone statement."),
                        fieldWithPath("objects[]").description("The ordered list of object position instances used in the rosetta stone statement. The order of the objects corresponds to the order of the properties of the rosetta stone template."),
                        fieldWithPath("objects[][]").description("The ordered list of object instance IDs used for the object position index defined by the outer array."),
                        fieldWithPath("certainty").description("""The certainty of the rosetta stone statement. Either of ${Certainty.entries.joinToString { "\"$it\"" }}."""),
                        fieldWithPath("resources").description("Definition of resources that need to be created."),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created."),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("classes").description("Definition of classes that need to be created."),
                        fieldWithPath("classes.*.label").description("The label of the class."),
                        fieldWithPath("classes.*.uri").description("The uri of the class."),
                        fieldWithPath("negated").description("Whether the statement represented by the rosetta stone statement instance is semantically negated. (optional, default: false)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the rosetta stone statement. Either of ${ExtractionMethod.entries.joinToString { "\"$it\"" }}."""),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created rosetta stone statement can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("subjects[]").description("The ordered list of subject instance IDs used in the updated rosetta stone statement."),
                        fieldWithPath("objects[]").description("The ordered list of object position instances used in the updated rosetta stone statement. The order of the objects corresponds to the order of the properties of the rosetta stone template."),
                        fieldWithPath("objects[][]").description("The ordered list of object instance IDs used for the object position index defined by the outer array."),
                        fieldWithPath("certainty").description("""The certainty of the updated rosetta stone statement. Either of ${Certainty.entries.joinToString { "\"$it\"" }}."""),
                        fieldWithPath("resources").description("Definition of resources that need to be created."),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created."),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("classes").description("Definition of classes that need to be created."),
                        fieldWithPath("classes.*.label").description("The label of the class."),
                        fieldWithPath("classes.*.uri").description("The uri of the class."),
                        fieldWithPath("negated").description("Whether the statement represented by the updated rosetta stone statement instance is semantically negated. (optional, default: false)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the updated rosetta stone statement. Either of ${ExtractionMethod.entries.joinToString { "\"$it\"" }}."""),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone statement, when soft deleting and service succeeds, then status is 204 NO CONTENT")
    fun softDelete() {
        val id = ThingId("R123")
        every { statementService.softDelete(id, any()) } just runs

        documentedDeleteRequestTo("/api/rosetta-stone/statements/{id}", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the rosetta stone statement to soft-delete.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.softDelete(id, ContributorId(MockUserId.USER)) }
    }

    private fun createRosettaStoneStatementRequest() =
        RosettaStoneStatementController.CreateRosettaStoneStatementRequest(
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
                "#temp1" to ResourceDefinitionDTO(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to LiteralDefinitionDTO("0.1", Literals.XSD.DECIMAL.prefixedUri)
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinitionDTO(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinitionDTO(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to ClassDefinitionDTO(
                    label = "class",
                    uri = URI.create("https://orkg.org/class/C1")
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
        RosettaStoneStatementController.UpdateRosettaStoneStatementRequest(
            subjects = listOf("R258", "R369", "#temp1"),
            objects = listOf(
                listOf("R987", "R654", "#temp2", "#temp3"),
                listOf("R321", "R741", "#temp4", "#temp5")
            ),
            certainty = Certainty.HIGH,
            negated = false,
            resources = mapOf(
                "#temp1" to ResourceDefinitionDTO(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to LiteralDefinitionDTO("0.1", Literals.XSD.DECIMAL.prefixedUri)
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinitionDTO(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinitionDTO(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to ClassDefinitionDTO(
                    label = "class",
                    uri = URI.create("https://orkg.org/class/C1")
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
