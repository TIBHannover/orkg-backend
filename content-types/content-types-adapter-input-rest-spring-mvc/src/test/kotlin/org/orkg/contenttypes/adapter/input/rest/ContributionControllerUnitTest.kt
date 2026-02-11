package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.contenttypes.adapter.input.rest.ContributionController.CreateContributionRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart.StatementObjectRequest
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.testing.fixtures.createContribution
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.constributionRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.contributionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateListRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateLiteralRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreatePredicateRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateResourceRequestPartRequestFields
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.testing.andExpectContribution
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [ContributionController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ContributionController::class])
internal class ContributionControllerUnitTest : MockMvcBaseTest("contributions") {
    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Test
    @DisplayName("Given a contribution, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned")
    fun findById() {
        val contribution = createContribution()
        every { contributionService.findById(contribution.id) } returns Optional.of(contribution)

        documentedGetRequestTo("/api/contributions/{id}", contribution.id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectContribution()
            .andDocument {
                summary("Fetching contributions")
                description(
                    """
                    A `GET` request provides information about a contribution.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the contribution to retrieve."),
                )
                responseFields<ContributionRepresentation>(contributionResponseFields())
                throws(ContributionNotFound::class)
            }

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
    fun findAll() {
        val contribution = createContribution()
        every { contributionService.findAll(any()) } returns pageOf(contribution)

        documentedGetRequestTo("/api/contributions")
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContribution("$.content[*]")
            .andDocument {
                summary("Listing contributions")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<contributions-fetch,contributions>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters()
                pagedResponseFields<ContributionRepresentation>(contributionResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
            .andDocument {
                summary("Creating contributions")
                description(
                    """
                    A `POST` request creates a new contribution with all the given parameters.
                    The response will be `201 Created` when successful.
                    The contribution (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created contribution can be fetched from."),
                )
                requestFields<CreateContributionRequest>(
                    fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of $allowedExtractionMethodValues. (default: `UNKNOWN`)""").optional(),
                    *mapOfCreateResourceRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateLiteralRequestPartRequestFields().toTypedArray(),
                    *mapOfCreatePredicateRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateListRequestPartRequestFields().toTypedArray(),
                    fieldWithPath("contribution").description("The definition of the contribution."),
                    *applyPathPrefix("contribution.", constributionRequestPartRequestFields()).toTypedArray(),
                )
                throws(
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    PaperNotFound::class,
                    PaperNotModifiable::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLabel::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    EmptyContribution::class,
                    ThingIsNotAPredicate::class,
                    InvalidStatementSubject::class,
                )
            }

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
        val exception = PaperNotFound.withId(ThingId("R123"))
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
        CreateContributionRequest(
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
