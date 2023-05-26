package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.application.LiteralController.LiteralCreateRequest
import eu.tib.orkg.prototype.statements.application.LiteralController.LiteralUpdateRequest
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [LiteralController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [LiteralController::class])
@DisplayName("Given a Literal controller")
class LiteralControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var literalService: LiteralUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun whenPOST_AndLabelIsEmptyString_ThenSucceed() {
        val literal = createCreateRequestWithEmptyLabel()
        val mockResult = Literal(
            id = ThingId("L1"),
            label = literal.label,
            datatype = literal.datatype,
            createdAt = OffsetDateTime.now()
        )
        every { literalService.findById(any()) } returns Optional.of(mockResult)
        every { literalService.create(any(), any(), any()) } returns mockResult

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/literals/L1"))

        verify(exactly = 1) {
            literalService.create(ContributorId("f2d66c90-3cbf-4d4f-951f-0fc470f682c4"), "", "xsd:string")
        }
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
    @WithMockUser(username = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4")
    fun whenPOST_AndRequestIsValid_ThenSucceed() {
        val literal = LiteralCreateRequest(
            label = "irrelevant",
            datatype = "irrelevant"
        )
        val mockResult = Literal(
            id = ThingId("L1"),
            label = literal.label,
            datatype = literal.datatype,
            createdAt = OffsetDateTime.now()
        )
        every { literalService.findById(any()) } returns Optional.of(mockResult)
        every { literalService.create(any(), any(), any()) } returns mockResult

        mockMvc
            .perform(creationOf(literal))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/literals/L1"))

        verify(exactly = 1) {
            literalService.create(ContributorId("f2d66c90-3cbf-4d4f-951f-0fc470f682c4"), "irrelevant", "irrelevant")
        }
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
