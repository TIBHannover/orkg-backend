package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.adapter.input.rest.LiteralController.CreateLiteralRequest
import org.orkg.graph.adapter.input.rest.LiteralController.UpdateLiteralRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.literalResponseFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.LiteralAlreadyExists
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectLiteral
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [LiteralController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [LiteralController::class])
internal class LiteralControllerUnitTest : MockMvcBaseTest("literals") {
    @MockkBean
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun findById() {
        val id = ThingId("L1234")
        val literal = createLiteral(
            id = id,
            createdAt = OffsetDateTime.of(2023, 6, 1, 15, 19, 4, 778631092, ZoneOffset.ofHours(2))
        )
        every { literalService.findById(any()) } returns Optional.of(literal)

        documentedGetRequestTo("/api/literals/{id}", id)
            .perform()
            .andExpect(status().isOk)
            // Explicitly test all properties of the representation. This works as serialization test.
            .andExpect(jsonPath("$.id", `is`("L1234")))
            .andExpect(jsonPath("$.label", `is`("Default Label")))
            .andExpect(jsonPath("$.datatype", `is`("xsd:string")))
            .andExpect(jsonPath("$.created_at", `is`("2023-06-01T15:19:04.778631092+02:00")))
            .andExpect(jsonPath("$.created_by", `is`("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1")))
            .andExpect(jsonPath("$.modifiable", `is`(true)))
            .andExpect(jsonPath("$._class", `is`("literal")))
            // Document the representation for later reference.
            .andDocument {
                summary("Fetching literals")
                description(
                    """
                    A `GET` request provides information about a literal.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the literal.")
                )
                responseFields<LiteralRepresentation>(
                    // The order here determines the order in the generated table. More relevant items should be up.
                    fieldWithPath("id").description("The identifier of the literal."),
                    fieldWithPath("label").description("The literal value."),
                    fieldWithPath("datatype").description("The datatype of the literal. Can be a (prefixed) URI."),
                    timestampFieldWithPath("created_at", "the literal was created"),
                    // TODO: Add links to documentation of special user UUIDs.
                    fieldWithPath("created_by").description("The UUID of the user or service who created this literal."),
                    fieldWithPath("modifiable").description("Whether this literal can be modified."),
                    fieldWithPath("_class").description("An indicator which type of entity was returned. Always has the value \"`literal`\".")
                )
                throws(LiteralNotFound::class)
            }

        verify(exactly = 1) { literalService.findById(any()) }
    }

    @Test
    @DisplayName("Given several literals, when filtering by no parameters, then status is 200 OK and literals are returned")
    fun getPaged() {
        every { literalService.findAll(any()) } returns pageOf(createLiteral())

        documentedGetRequestTo("/api/literals")
            .accept(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteral("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literalService.findAll(any()) }
    }

    @Test
    @DisplayName("Given several literals, when they are fetched with all possible filtering parameters, then status is 200 OK and literals are returned")
    fun findAll() {
        every { literalService.findAll(any(), any(), any(), any(), any()) } returns pageOf(createLiteral())

        val label = "label"
        val exact = true
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)

        documentedGetRequestTo("/api/literals")
            .param("q", label)
            .param("exact", exact.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .accept(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteral("$.content[*]")
            .andDocument {
                summary("Listing literals")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<literals-fetch,literals>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this literal. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned literal can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned literal can have. (optional)").optional(),
                )
                pagedResponseFields<LiteralRepresentation>(literalResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            literalService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
            )
        }
    }

    @Test
    fun `Given several literals, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { literalService.findAll(any()) } throws exception

        get("/api/literals")
            .param("sort", "unknown")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) { literalService.findAll(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When creating a literal, and request is valid, then status is 201 CREATED")
    fun create() {
        val id = ThingId("L1")
        val literal = createLiteral(id)
        val request = CreateLiteralRequest(
            label = literal.label,
            datatype = literal.datatype
        )
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = request.label,
            datatype = request.datatype,
        )
        every { literalService.create(command) } returns literal.id

        documentedPostRequestTo("/api/literals")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/literals/$id")))
            .andDocument {
                summary("Creating literals")
                description(
                    """
                    A `POST` request creates a new literal with a given label (its value).
                    The response will be `201 Created` when successful.
                    The literal can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created literal can be fetched from.")
                )
                requestFields<CreateLiteralRequest>(
                    fieldWithPath("label").description("The value of the literal."),
                    fieldWithPath("datatype").description("The datatype of the literal value. (default: xsd:string)")
                )
                throws(InvalidLiteralLabel::class, InvalidLiteralLabel::class, InvalidLiteralDatatype::class, LiteralAlreadyExists::class)
            }

        verify(exactly = 1) { literalService.create(command) }
    }

    @Test
    @TestWithMockUser
    fun whenPOST_AndLabelIsEmptyString_ThenSucceed() {
        val literal = createCreateRequestWithEmptyLabel()
        val mockResult = Literal(
            id = ThingId("L1"),
            label = literal.label,
            datatype = literal.datatype,
            createdAt = OffsetDateTime.now(clock),
            createdBy = ContributorId(MockUserId.USER)
        )
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "",
            datatype = "xsd:string"
        )
        every { literalService.create(command) } returns mockResult.id

        post("/api/literals")
            .content(literal)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", matchesPattern("https?://.+/api/literals/L1")))

        verify(exactly = 1) { literalService.create(command) }
    }

    @Test
    fun whenPOST_AndDatatypeIsBlank_ThenFailValidation() {
        val literal = CreateLiteralRequest(
            label = "irrelevant",
            datatype = " ".repeat(5)
        )

        post("/api/literals")
            .content(literal)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_argument")
    }

    @Test
    @TestWithMockUser
    fun whenPOST_AndLabelIsTooLong_ThenFailValidation() {
        val literal = CreateLiteralRequest(
            label = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        every { literalService.create(any()) } throws InvalidLiteralLabel()

        post("/api/literals")
            .content(literal)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal_label")

        verify(exactly = 1) { literalService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literal update command, when service succeeds, it returns status 204 NO CONTENT")
    fun update() {
        val literal = createLiteral(label = "foo")
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = literal.id,
            contributorId = ContributorId(MockUserId.USER),
            label = literal.label,
            datatype = Literals.XSD.STRING.prefixedUri
        )
        val request = mapOf(
            "label" to literal.label,
            "datatype" to literal.datatype,
        )

        every { literalService.update(command) } just runs

        documentedPutRequestTo("/api/literals/{id}", literal.id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literals/${literal.id}")))
            .andDocument {
                summary("Updating literals")
                description(
                    """
                    A `PUT` request updates a literal with the given parameters.
                    The response will be `204 NO CONTENT` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the literal.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literal can be fetched from.")
                )
                requestFields<UpdateLiteralRequest>(
                    fieldWithPath("label").description("The updated value of the literal. (optional)").optional(),
                    fieldWithPath("datatype").description("The updated datatype of the literal value. (optional)").optional(),
                )
                throws(InvalidLiteralLabel::class, LiteralNotFound::class, LiteralNotModifiable::class, InvalidLiteralLabel::class, InvalidLiteralDatatype::class)
            }

        verify(exactly = 1) { literalService.update(command) }
    }

    private fun createCreateRequestWithEmptyLabel() = CreateLiteralRequest(label = "")

    private fun createLiteral(): Literal = Literal(
        id = ThingId("L1"),
        label = "irrelevant",
        datatype = "irrelevant",
        createdAt = OffsetDateTime.now(clock)
    )
}
