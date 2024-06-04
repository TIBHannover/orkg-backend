package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Predicate Controller")
@Transactional
@Import(MockUserDetailsService::class)
class PredicateControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @BeforeEach
    fun setup() {
        service.removeAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    fun index() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "knows")

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
                    listOfPredicatesResponseFields3()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.createPredicate(label = "has name")

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
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "knows")

        mockMvc
            .perform(getRequestTo("/api/predicates/?q=name"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    listOfPredicatesResponseFields3()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "know(s)")

        mockMvc
            .perform(getRequestTo("/api/predicates/?q=know("))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    listOfPredicatesResponseFields3()
                )
            )
    }

    @Test
    @WithMockUser
    fun edit() {
        val predicate = service.createPredicate(label = "knows")

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

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateNotFound() {
        mockMvc
            .perform(deleteRequest("/api/predicates/NONEXISTENT"))
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateSuccess() {
        val id = service.createPredicate(label = "bye bye", contributorId = ContributorId(MockUserId.ADMIN))

        mockMvc
            .perform(deleteRequest("/api/predicates/$id"))
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateForbidden() {
        val subject = resourceService.createResource(label = "subject")
        val `object` = resourceService.createResource(label = "child")
        val predicate = service.createPredicate(label = "related")
        statementService.create(subject, predicate, `object`)

        mockMvc
            .perform(deleteRequest("/api/predicates/$predicate"))
            .andExpect(status().isForbidden)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @Disabled("throwing an exception with the message (An Authentication object was not found in the SecurityContext)")
    fun deletePredicateWithoutLogin() {
        val id = service.createPredicate(label = "To Delete")

        mockMvc
            .perform(deleteRequest("/api/predicates/$id"))
            .andExpect(status().isUnauthorized)
            .andDo(
                document(
                    snippet
                )
            )
    }

    fun listOfPredicatesResponseFields3(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", predicateResponseFields())

    companion object RestDoc {
        fun predicateResponseFields() = listOf(
            fieldWithPath("id").description("The predicate ID"),
            fieldWithPath("label").description("The predicate label"),
            fieldWithPath("created_at").description("The predicate creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
            fieldWithPath("description").description("The description of the predicate, if exists.").optional(),
            fieldWithPath("_class").description("Class description").optional().ignored(),
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored(),
            fieldWithPath("modifiable").description("Whether this predicate can be modified.").optional().ignored(),
        )
    }
}
