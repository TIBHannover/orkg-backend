package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.LiteralController.LiteralCreateRequest
import org.orkg.graph.adapter.input.rest.LiteralController.LiteralUpdateRequest
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.orkg.graph.testing.fixtures.withLiteralIds
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LiteralController::class, ExceptionHandler::class, CommonJacksonModule::class, GraphJacksonModule::class])
@WebMvcTest(controllers = [LiteralController::class])
@DisplayName("Given a Literal controller")
@UsesMocking
internal class LiteralControllerUnitTest : RestDocsTest("literals") {

    @MockkBean
    private lateinit var literalService: LiteralUseCases

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
                        fieldWithPath("_class").description("An indicator which type of entity was returned. Always has the value \"`literal`\".")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @DisplayName("correctly serializes multiple literals as page")
    fun getPaged() {
        // Keep the collection size small, because the output ends up in the doc. Use a seed to keep output stable.
        val config = FabricatorConfig(seed = 1, collectionSizes = 3..3).withStandardMappings()
        val literals = Fabrikate(config).withCustomMappings().withLiteralIds().random<List<Literal>>()
        // Pretend we wanted to obtain only 3 elements, but 15 are available:
        val page: Page<Literal> = PageImpl(literals, PageRequest.of(0, literals.size), 15)
        every { literalService.findAll(any()) } returns page

        mockMvc.perform(documentedGetRequestTo("/api/literals/"))
            .andExpect(status().isOk)
            .andExpectPage()
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
            createdAt = OffsetDateTime.now(),
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
            createdAt = OffsetDateTime.now(),
            createdBy = ContributorId(MockUserId.USER)
        )
        val command = CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = literal.label,
            datatype = literal.datatype
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
        MockMvcRequestBuilders.post("/api/literals/")
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(literal))

    private fun updateOf(literal: LiteralUpdateRequest, id: String) =
        MockMvcRequestBuilders.put("/api/literals/$id")
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(literal))

    private fun createCreateRequestWithBlankLabel() = LiteralCreateRequest(label = " ".repeat(5))

    private fun createCreateRequestWithEmptyLabel() = LiteralCreateRequest(label = "")

    private fun createUpdateRequestWithBlankLabel() =
        LiteralUpdateRequest(id = null, label = " ".repeat(5), datatype = null)

    private fun createUpdateRequestWithEmptyLabel() =
        LiteralUpdateRequest(id = null, label = "", datatype = null)

    private fun createUpdateRequestWithBlankDatatype() =
        LiteralUpdateRequest(id = null, label = null, datatype = " ".repeat(5))

    private fun createDummyLiteral(): Literal {
        return Literal(
            id = ThingId("L1"),
            label = "irrelevant",
            datatype = "irrelevant",
            createdAt = OffsetDateTime.now()
        )
    }
}
