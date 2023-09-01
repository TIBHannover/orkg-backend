package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.pageOf
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.testing.andExpectPage
import eu.tib.orkg.prototype.testing.andExpectStatement
import eu.tib.orkg.prototype.testing.annotations.UsesMocking
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedDeleteRequestTo
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedPutRequestTo
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.security.Principal
import java.util.*
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.statements.testing.createPredicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [BulkStatementController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [BulkStatementController::class])
@UsesMocking
internal class BulkStatementControllerUnitTest : RestDocsTest("bulk-statements") {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var templateRepository: TemplateRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, templateRepository, flags, userRepository)
    }

    @Test
    fun lookupBySubjects() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(createResource(r1), createPredicate(p1), createResource(r2))
            .copy(id = StatementId("S1"))
        val s2 = createStatement(createResource(r3), createPredicate(p2), createResource(r4))
            .copy(id = StatementId("S2"))
        val pageable = PageRequest.of(0, 5)

        every { statementService.findAllBySubject(r1, any()) } returns pageOf(s1, pageable = pageable)
        every { statementService.findAllBySubject(r3, any()) } returns pageOf(s2, pageable = pageable)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc
            .perform(documentedGetRequestTo("/api/statements/subjects/?ids={ids}", "$r1,$r3"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDo(
                documentationHandler.document(
                    requestParameters(parameterWithName("ids").description("The list of resource iIds to fetch on")),
                    responseFields(
                        fieldWithPath("[].id").description("The subject id that was used to fetch the following statements"),
                        subsectionWithPath("[].statements").description("Page of statements whose subject id matches the id from the search parameter"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAllBySubject(r1, any())
            statementService.findAllBySubject(r3, any())
        }
        verify(exactly = 2) {
            statementService.countStatementsAboutResources(any())
            flags.isFormattedLabelsEnabled()
        }
    }

    @Test
    fun lookupByObjects() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(createResource(r1), createPredicate(p1), createResource(r2))
            .copy(id = StatementId("S1"))
        val s2 = createStatement(createResource(r3), createPredicate(p2), createResource(r4))
            .copy(id = StatementId("S2"))
        val pageable = PageRequest.of(0, 5)

        every { statementService.findAllByObject(r2, any()) } returns pageOf(s1, pageable = pageable)
        every { statementService.findAllByObject(r4, any()) } returns pageOf(s2, pageable = pageable)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc
            .perform(documentedGetRequestTo("/api/statements/objects/?ids={ids}", "$r2,$r4"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDo(
                documentationHandler.document(
                    requestParameters(parameterWithName("ids").description("The list of object ids to fetch on")),
                    responseFields(
                        fieldWithPath("[].id").description("The object id that was used to fetch the following statements"),
                        subsectionWithPath("[].statements").description("Page of statements whose object id matches the id from the search parameter"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAllByObject(r2, any())
            statementService.findAllByObject(r4, any())
        }
        verify(exactly = 2) {
            statementService.countStatementsAboutResources(any())
            flags.isFormattedLabelsEnabled()
        }
    }

    @Test
    fun editResourceStatements() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(createResource(r1), createPredicate(p1), createResource(r2))
            .copy(id = StatementId("S1"))
        val s2 = createStatement(createResource(r3), createPredicate(p2), createResource(r4))
            .copy(id = StatementId("S2"))

        val newP = createPredicate(ThingId("P3"))
        val newO = createResource(ThingId("R5"))

        val newS1 = createStatement(s1.subject, newP, newO).copy(id = s1.id)
        val newS2 = createStatement(s2.subject, newP, newO).copy(id = s2.id)

        val payload = objectMapper.writeValueAsString(
            mapOf(
                "predicate_id" to newP.id,
                "object_id" to newO.id,
            )
        )

        every { statementService.update(match { it.statementId == s1.id || it.statementId == s2.id }) } just runs
        every { statementService.findById(s1.id!!) } returns Optional.of(newS1)
        every { statementService.findById(s2.id!!) } returns Optional.of(newS2)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc
            .perform(documentedPutRequestTo("/api/statements/?ids={ids}", "${s1.id},${s2.id}", body = payload))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectStatement("$[*].statement")
            .andDo(
                documentationHandler.document(
                    requestParameters(parameterWithName("ids").description("The list of statements to update")),
                    responseFields(
                        fieldWithPath("[].id").description("The statement id"),
                        subsectionWithPath("[].statement").description("The statement representation of the updated statement"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.update(match { it.statementId == s1.id })
            statementService.update(match { it.statementId == s2.id })
            statementService.findById(s1.id!!)
            statementService.findById(s2.id!!)
            statementService.countStatementsAboutResources(any())
            flags.isFormattedLabelsEnabled()
        }
    }

    @Test
    fun delete() {
        val s1 = StatementId("S1")
        val s2 = StatementId("S2")
        val principal: Principal = mockk()

        every { statementService.delete(setOf(s1, s2)) } just runs
        every { principal.name } returns "user"

        mockMvc
            .perform(documentedDeleteRequestTo("/api/statements/?ids={ids}", "$s1,$s2").principal(principal))
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    requestParameters(parameterWithName("ids").description("The list of ids of statements to delete"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.delete(setOf(s1, s2)) }
        verify(exactly = 2) { principal.name }

        confirmVerified(principal)
    }
}
