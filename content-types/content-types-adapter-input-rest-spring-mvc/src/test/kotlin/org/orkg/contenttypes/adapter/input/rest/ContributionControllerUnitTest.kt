package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart.StatementObjectRequest
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.testing.fixtures.createContribution
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.testing.andExpectContribution
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        ContributionController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [ContributionController::class])
internal class ContributionControllerUnitTest : MockMvcBaseTest("contributions") {
    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Test
    @DisplayName("Given a contribution, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned")
    fun getSingle() {
        val contribution = createContribution()
        every { contributionService.findById(contribution.id) } returns Optional.of(contribution)

        documentedGetRequestTo("/api/contributions/{id}", contribution.id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectContribution()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contribution to retrieve.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the contribution."),
                        fieldWithPath("label").description("The label of the contribution."),
                        fieldWithPath("classes").description("The classes of the contribution resource."),
                        subsectionWithPath("properties").description("A map of predicate ids to lists of thing ids, that represent the statements that this contribution consists of."),
                        fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of $allowedExtractionMethodValues."""),
                        timestampFieldWithPath("created_at", "the contribution resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this contribution."),
                        fieldWithPath("visibility").description("""Visibility of the contribution. Can be one of $allowedVisibilityValues."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this contribution.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributionService.findById(contribution.id) }
    }

    @Test
    fun `Given a contribution, when it is fetched by id and service reports missing contribution, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { contributionService.findById(id) } returns Optional.empty()

        get("/api/contributions/{id}", id)
            .accept(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:contribution_not_found")

        verify(exactly = 1) { contributionService.findById(id) }
    }

    @Test
    @DisplayName("Given several contributions, then status is 200 OK and contributions are returned")
    fun getPaged() {
        val contribution = createContribution()
        every { contributionService.findAll(any()) } returns pageOf(contribution)

        documentedGetRequestTo("/api/contributions")
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContribution("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributionService.findAll(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contribution request, when service succeeds, it creates and returns the contribution")
    fun create() {
        val paperId = ThingId("R3541")
        val contributionId = ThingId("R123")
        every { contributionService.create(any()) } returns contributionId

        documentedPostRequestTo("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/contributions/$contributionId")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created contribution can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of $allowedExtractionMethodValues. (default: "UNKNOWN")""").optional(),
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
                        fieldWithPath("contribution").description("List of definitions of contribution that need to be created."),
                        fieldWithPath("contribution.label").description("Label of the contribution."),
                        fieldWithPath("contribution.classes").description("The classes of the contribution resource."),
                        subsectionWithPath("contribution.statements").description("Recursive map of statements contained within the contribution."),
                        fieldWithPath("contribution.statements.*[].id").description("The ID of the object of the statement.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidTempId("invalid")
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_temp_id")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:duplicate_temp_ids")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = PaperNotFound(ThingId("R123"))
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingNotDefined("R123")
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_not_defined")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = ThingNotFound("R123")
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:thing_not_found")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_class")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAPredicate("R123")
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_predicate")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidStatementSubject("R123")
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement_subject")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports empty contribution, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = EmptyContribution()
        every { contributionService.create(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_contribution")

        verify(exactly = 1) { contributionService.create(any()) }
    }

    private fun createContributionRequest() =
        ContributionController.CreateContributionRequest(
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to CreateLiteralRequestPart(
                    label = "0.1",
                    dataType = "xsd:decimal"
                )
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
            contribution = ContributionRequestPart(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    "P32" to listOf(
                        StatementObjectRequest(
                            id = "R3003",
                            statements = null
                        )
                    ),
                    "HAS_EVALUATION" to listOf(
                        StatementObjectRequest(
                            id = "#temp1",
                            statements = null
                        ),
                        StatementObjectRequest(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    StatementObjectRequest(
                                        id = "R3003",
                                        statements = null
                                    ),
                                    StatementObjectRequest(
                                        id = "#temp2",
                                        statements = null
                                    ),
                                    StatementObjectRequest(
                                        id = "#temp4",
                                        statements = null
                                    )
                                ),
                                "P32" to listOf(
                                    StatementObjectRequest(
                                        id = "#temp2",
                                        statements = null
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
}
