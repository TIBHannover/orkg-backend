package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Stats Controller")
@Transactional
class StatsControllerTest : RestDocumentationBaseTest() {

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

        resourceService.removeAll()
        predicateService.removeAll()
        literalService.removeAll()
        classService.removeAll()

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

        predicateService.createPredicates(
            "P32" to "has research problem",
            "P31" to "has contribution"
        )
        predicateService.createPredicate(label = "DOI")
        predicateService.createPredicate(label = "yields")
        literalService.createLiteral(label = "Springer")
        literalService.createLiteral(label = "ORKG rocks")
        literalService.createLiteral(label = "Out of this world")
        literalService.createLiteral(label = "We are crazy")

        mockMvc.perform(getRequestTo("/api/stats/")).andExpect(status().isOk).andDo(
            document(
                snippet, statsResponseFields()
            )
        )
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
        fieldWithPath("extras").description("A dictionary with on-the-fly classes and their corresponding numbers")
            .optional(),
    )
}
