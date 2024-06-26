package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPredicate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        PredicateController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        GraphJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [PredicateController::class])
@DisplayName("Given a Predicate controller")
@UsesMocking
internal class PredicateControllerUnitTest : RestDocsTest("predicates") {

    @MockkBean
    private lateinit var predicateService: PredicateUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate create request, when service succeeds, it creates and returns the predicate")
    fun create() {
        val request = PredicateController.CreatePredicateRequest(
            id = null,
            label = "predicate label"
        )
        val id = ThingId("R123")
        val predicate = createPredicate(id, label = request.label)

        every { predicateService.create(any()) } returns id
        every { predicateService.findById(id) } returns Optional.of(predicate)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        post("/api/predicates/")
            .content(request)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))
            .andExpectPredicate()
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("id").type("String").description("The id of the predicate. (optional)").optional(),
                        fieldWithPath("label").description("The label of the predicate.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The predicate id."),
                        fieldWithPath("label").description("The predicate label."),
                        timestampFieldWithPath("created_at", "the predicate  was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
                        fieldWithPath("description").type("String").description("The description of the predicate, if exists.").optional(),
                        fieldWithPath("modifiable").description("Whether this predicate can be modified."),
                        fieldWithPath("_class").description("Class description, always `predicate`.").optional().ignored()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            predicateService.create(
                withArg {
                    it.id shouldBe request.id
                    it.label shouldBe request.label
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                }
            )
            predicateService.findById(id)
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a predicate create request, when id is specified and service succeeds, it creates and returns the predicate`() {
        val id = ThingId("R123")
        val request = PredicateController.CreatePredicateRequest(
            id = id,
            label = "predicate label"
        )
        val predicate = createPredicate(id, label = request.label)

        every { predicateService.create(any()) } returns id
        every { predicateService.findById(id) } returns Optional.of(predicate)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        post("/api/predicates/", request)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))
            .andExpectPredicate()

        verify(exactly = 1) {
            predicateService.create(
                withArg {
                    it.id shouldBe request.id
                    it.label shouldBe request.label
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                }
            )
            predicateService.findById(id)
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }
}
