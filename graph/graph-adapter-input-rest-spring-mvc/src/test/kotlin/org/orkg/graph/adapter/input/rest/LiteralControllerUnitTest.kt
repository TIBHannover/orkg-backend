package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.LiteralController.LiteralCreateRequest
import org.orkg.graph.adapter.input.rest.LiteralController.LiteralUpdateRequest
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectLiteral
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LiteralController::class, ExceptionHandler::class, CommonJacksonModule::class, GraphJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LiteralController::class])
@DisplayName("Given a Literal controller")
@UsesMocking
internal class LiteralControllerUnitTest : RestDocsTest("literals") {

    @MockkBean
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @DisplayName("correctly serializes an existing literal")
    fun getSingle() {
        val id = ThingId("L1234")
        val literal = createLiteral(
            id = id,
            createdAt = OffsetDateTime.of(2023, 6, 1, 15, 19, 4, 778631092, ZoneOffset.ofHours(2))
        )
        every { literalService.findById(any()) } returns Optional.of(literal)

        mockMvc.perform(documentedGetRequestTo("/api/literals/{id}", id))
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
            .andDo(
                documentationHandler.document(
                    pathParameters(parameterWithName("id").description("The identifier of the literal to retrieve.")),
                    responseFields(
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
                )
            )
            .andDo(generateDefaultDocSnippets())
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
        every { literalService.findById(any()) } returns Optional.of(mockResult)
        every { literalService.create(command) } returns mockResult.id

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", matchesPattern("https?://.+/api/literals/L1")))

        verify(exactly = 1) { literalService.create(command) }
    }

    @Test
    @DisplayName("Given several literals, when filtering by no parameters, then status is 200 OK and literals are returned")
    fun getPaged() {
        every { literalService.findAll(any()) } returns pageOf(createLiteral())

        documentedGetRequestTo("/api/literals")
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteral("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literalService.findAll(any()) }
    }

    @Test
    @DisplayName("Given several literals, when they are fetched with all possible filtering parameters, then status is 200 OK and literals are returned")
    fun getPagedWithParameters() {
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
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteral("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)"),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this literal. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned literal can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned literal can have. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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

        mockMvc.perform(get("/api/literals?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/literals"))
    }

    @Test
    @TestWithMockUser
    fun whenPUT_AndLabelIsEmpty_ThenSucceed() {
        val literal = createUpdateRequestWithEmptyLabel()
        val double = createDummyLiteral() // needed so "expected" has the same timestamp
        val expected = double.copy(label = "")
        every { literalService.findById(any()) } returns Optional.of(double) andThen Optional.of(expected)
        every { literalService.update(any()) } just runs

        mockMvc
            .perform(updateOf(literal, "L1"))
            .andExpect(status().isOk)

        verify(exactly = 1) {
            literalService.update(expected)
        }
    }

    @Test
    fun whenPOST_AndDatatypeIsBlank_ThenFailValidation() {
        val literal = LiteralCreateRequest(
            label = "irrelevant",
            datatype = " ".repeat(5)
        )

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors.length()").value(1))
            .andExpect(jsonPath("\$.errors[0].field").value("datatype"))
            .andExpect(jsonPath("\$.errors[0].message").value("must not be blank"))
    }

    @Test
    @TestWithMockUser
    fun whenPUT_AndDatatypeIsBlank_ThenFailValidation() {
        val literal = createUpdateRequestWithBlankDatatype()
        every { literalService.findById(any()) } returns Optional.of(createDummyLiteral())

        mockMvc
            .perform(updateOf(literal, "L1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors.length()").value(1))
            .andExpect(jsonPath("\$.errors[0].field").value("datatype"))
            .andExpect(jsonPath("\$.errors[0].message").value("must not be blank"))
    }

    @Test
    @TestWithMockUser
    fun whenPOST_AndLabelIsTooLong_ThenFailValidation() {
        val literal = LiteralCreateRequest(
            label = "a".repeat(MAX_LABEL_LENGTH + 1)
        )
        every { literalService.create(any()) } throws InvalidLiteralLabel()

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors.length()").value(1))
            .andExpect(jsonPath("\$.errors[0].field").value("label"))
            .andExpect(jsonPath("\$.errors[0].message").value("A literal must be at most $MAX_LABEL_LENGTH characters long."))

        verify(exactly = 1) { literalService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun whenPUT_AndLabelIsTooLong_ThenFailValidation() {
        val literal = LiteralUpdateRequest(
            id = null,
            label = "a".repeat(MAX_LABEL_LENGTH + 1),
            datatype = null
        )
        every { literalService.findById(any()) } returns Optional.of(createDummyLiteral())
        every { literalService.update(any()) } throws InvalidLiteralLabel()

        mockMvc
            .perform(updateOf(literal, "L1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors.length()").value(1))
            .andExpect(jsonPath("\$.errors[0].field").value("label"))
            .andExpect(jsonPath("\$.errors[0].message").value("A literal must be at most $MAX_LABEL_LENGTH characters long."))
    }

    @Test
    @TestWithMockUser
    fun whenPOST_AndRequestIsValid_ThenSucceed() {
        val literal = LiteralCreateRequest(
            label = "irrelevant",
            datatype = "irrelevant"
        )
        val mockResult = Literal(
            id = ThingId("L1"),
            label = literal.label,
            datatype = literal.datatype,
            createdAt = OffsetDateTime.now(clock),
            createdBy = ContributorId(MockUserId.USER),
        )
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = literal.label,
            datatype = literal.datatype,
        )
        every { literalService.findById(any()) } returns Optional.of(mockResult)
        every { literalService.create(command) } returns mockResult.id

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", matchesPattern("https?://.+/api/literals/L1")))

        verify(exactly = 1) { literalService.create(command) }
    }

    private fun creationOf(literal: LiteralCreateRequest) =
        MockMvcRequestBuilders.post("/api/literals")
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(literal))

    private fun updateOf(literal: LiteralUpdateRequest, id: String) =
        MockMvcRequestBuilders.put("/api/literals/{id}", id)
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(literal))

    private fun createCreateRequestWithEmptyLabel() = LiteralCreateRequest(label = "")

    private fun createUpdateRequestWithEmptyLabel() =
        LiteralUpdateRequest(id = null, label = "", datatype = null)

    private fun createUpdateRequestWithBlankDatatype() =
        LiteralUpdateRequest(id = null, label = null, datatype = " ".repeat(5))

    private fun createDummyLiteral(): Literal {
        return Literal(
            id = ThingId("L1"),
            label = "irrelevant",
            datatype = "irrelevant",
            createdAt = OffsetDateTime.now(clock)
        )
    }
}
