package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class StatisticsControllerIntegrationTest : MockMvcBaseTest("statistics") {
    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.deleteAll()
        predicateService.deleteAll()
        literalService.deleteAll()
        classService.deleteAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(literalService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun index() {
        classService.createClasses("Paper")
        classService.createClass(label = "Awesome class")
        resourceService.createResource(
            id = "R11",
            label = "Research field"
        )
        resourceService.createResource(label = "Python")
        resourceService.createResource(label = "C#")
        resourceService.createResource(classes = setOf("Paper"), label = "Paper 212")
        resourceService.createResource(classes = setOf("Paper"), label = "Paper 222")
        resourceService.createResource(classes = setOf("Paper"), label = "Paper 432")

        predicateService.createPredicate(Predicates.hasResearchProblem)
        predicateService.createPredicate(Predicates.hasContribution)

        predicateService.createPredicate(label = "DOI")
        predicateService.createPredicate(label = "yields")
        literalService.createLiteral(label = "Springer")
        literalService.createLiteral(label = "ORKG rocks")
        literalService.createLiteral(label = "Out of this world")
        literalService.createLiteral(label = "We are crazy")

        documentedGetRequestTo("/api/stats")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    statsResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    private fun statsResponseFields() = responseFields(
        fieldWithPath("statements").description("The number of statements"),
        fieldWithPath("resources").description("The number of resources"),
        fieldWithPath("predicates").description("The number of predicates"),
        fieldWithPath("literals").description("The number of literals"),
        fieldWithPath("papers").description("The number of papers"),
        fieldWithPath("classes").description("The number of classes"),
        fieldWithPath("contributions").description("The number of research contributions"),
        fieldWithPath("fields").description("The number of research fields"),
        fieldWithPath("problems").description("The number of research problems"),
        fieldWithPath("comparisons").description("The number of comparisons"),
        fieldWithPath("visualizations").description("The number of visualizations"),
        fieldWithPath("templates").description("The number of templates"),
        fieldWithPath("smart_reviews").description("The number of smart_reviews"),
        fieldWithPath("users").description("The number of users"),
        fieldWithPath("observatories").description("The number of observatories"),
        fieldWithPath("organizations").description("The number of organizations"),
        fieldWithPath("orphaned_nodes").description("The number of orphaned nodes"),
        fieldWithPath("extras").description("A map with on-the-fly classes and their corresponding numbers").optional(),
    )
}
