package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
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

        get("/api/predicates")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetch() {
        val id = service.createPredicate(label = "has name")

        documentedGetRequestTo("/api/predicates/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate.")
                    ),
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

        get("/api/predicates")
            .param("q", "name")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "know(s)")

        get("/api/predicates").param("q", "know(")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun edit() {
        val predicate = service.createPredicate(label = "knows")

        val newLabel = "yaser"
        val resource = mapOf("label" to newLabel)

        documentedPutRequestTo("/api/predicates/{id}", predicate)
            .content(resource)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate.")
                    ),
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
        delete("/api/predicates/{id}", "NONEXISTENT")
            .perform()
            .andExpect(status().isNotFound)
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateSuccess() {
        val id = service.createPredicate(label = "bye bye", contributorId = ContributorId(MockUserId.ADMIN))

        documentedDeleteRequestTo("/api/predicates/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deletePredicateForbidden() {
        val subject = resourceService.createResource(label = "subject")
        val `object` = resourceService.createResource(label = "child")
        val predicate = service.createPredicate(label = "related")
        statementService.create(subject, predicate, `object`)

        delete("/api/predicates/{id}", predicate)
            .perform()
            .andExpect(status().isForbidden)
    }

    @Test
    fun deletePredicateWithoutLogin() {
        val id = service.createPredicate(label = "To Delete")

        delete("/api/predicates/{id}", id)
            .perform()
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
