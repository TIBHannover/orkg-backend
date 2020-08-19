package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Predicate Controller")
@Transactional
@Import(MockUserDetailsService::class)
class PredicateControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: PredicateController

    @Autowired
    private lateinit var service: PredicateService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("has name")
        service.create("knows")

        mockMvc
            .perform(getRequestTo("/api/predicates/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("page").description("Page number of predicates to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of predicates to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional()
                    ),
                    listOfPredicatesResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("has name").id

        mockMvc
            .perform(getRequestTo("/api/predicates/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(predicateResponseFields())
                )
            )
    }

    @Test
    fun lookup() {
        service.create("has name")
        service.create("gave name to")
        service.create("knows")

        mockMvc
            .perform(getRequestTo("/api/predicates/?q=name"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    listOfPredicatesResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.create("has name")
        service.create("gave name to")
        service.create("know(s)")

        mockMvc
            .perform(getRequestTo("/api/predicates/?q=know("))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    listOfPredicatesResponseFields()
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun add() {
        val resource = mapOf("label" to "knows")

        mockMvc
            .perform(postRequestWithBody("/api/predicates/", resource))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The predicate label")
                    ),
                    createdResponseHeaders(),
                    responseFields(predicateResponseFields())
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addExistingId() {
        service.create(CreatePredicateRequest(id = PredicateId("dummy"), label = "foo"))
        val duplicatePredicate = mapOf("id" to "dummy", "label" to "bar")

        mockMvc
            .perform(postRequestWithBody("/api/predicates/", duplicatePredicate))
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("id").description("The predicate id"),
                        fieldWithPath("label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun edit() {
        val predicate = service.create("knows").id!!

        val newLabel = "yaser"
        val resource = mapOf("label" to newLabel)

        mockMvc
            .perform(putRequestWithBody("/api/predicates/$predicate", resource))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The updated predicate label")
                    ),
                    responseFields(predicateResponseFields())
                )
            )
    }

    companion object RestDoc {
        fun predicateResponseFields() = listOf(
            fieldWithPath("id").description("The predicate ID"),
            fieldWithPath("label").description("The predicate label"),
            fieldWithPath("created_at").description("The predicate creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
            fieldWithPath("description").description("The description of the predicate, if exists.").optional(),
            fieldWithPath("_class").optional().ignored()
        )

        fun listOfPredicatesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of predicates"))
                .andWithPrefix("[].", predicateResponseFields())
    }
}
