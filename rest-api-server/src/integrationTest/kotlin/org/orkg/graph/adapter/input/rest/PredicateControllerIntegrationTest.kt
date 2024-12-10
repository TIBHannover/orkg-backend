package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedDeleteRequestTo
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@DisplayName("Predicate Controller")
@Transactional
@Import(MockUserDetailsService::class)
internal class PredicateControllerIntegrationTest : RestDocsTest("predicates") {

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
            .perform(get("/api/predicates"))
            .andExpect(status().isOk)
    }

    @Test
    fun fetch() {
        val id = service.createPredicate(label = "has name")

        mockMvc
            .perform(documentedGetRequestTo("/api/predicates/{id}", id))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    responseFields(predicateResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookup() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "knows")

        mockMvc
            .perform(get("/api/predicates").param("q", "name"))
            .andExpect(status().isOk)
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "know(s)")

        mockMvc
            .perform(get("/api/predicates").param("q", "know("))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun edit() {
        val predicate = service.createPredicate(label = "knows")

        val newLabel = "yaser"
        val resource = mapOf("label" to newLabel)

        mockMvc
            .perform(documentedPutRequestTo("/api/predicates/{id}", predicate)
                .content(resource)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("label").description("The updated predicate label")
                    ),
                    responseFields(predicateResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateNotFound() {
        mockMvc
            .perform(delete("/api/predicates/{id}", "NONEXISTENT"))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateSuccess() {
        val id = service.createPredicate(label = "bye bye", contributorId = ContributorId(MockUserId.ADMIN))

        mockMvc
            .perform(documentedDeleteRequestTo("/api/predicates/{id}", id))
            .andExpect(status().isNoContent)
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateForbidden() {
        val subject = resourceService.createResource(label = "subject")
        val `object` = resourceService.createResource(label = "child")
        val predicate = service.createPredicate(label = "related")
        statementService.create(subject, predicate, `object`)

        mockMvc
            .perform(delete("/api/predicates/{id}", predicate))
            .andExpect(status().isForbidden)
    }

    @Test
    fun deletePredicateWithoutLogin() {
        val id = service.createPredicate(label = "To Delete")

        mockMvc
            .perform(delete("/api/predicates/{id}", id))
            .andExpect(status().isForbidden)
    }

    companion object RestDoc {
        fun predicateResponseFields() = listOf(
            fieldWithPath("id").description("The predicate ID"),
            fieldWithPath("label").description("The predicate label"),
            fieldWithPath("created_at").description("The predicate creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
            fieldWithPath("description").type("String").description("The description of the predicate, if exists.").optional(),
            fieldWithPath("_class").description("Class description").optional().ignored(),
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored(),
            fieldWithPath("modifiable").description("Whether this predicate can be modified.").optional().ignored(),
        )
    }
}
