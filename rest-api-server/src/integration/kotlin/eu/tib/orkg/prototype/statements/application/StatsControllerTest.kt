package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createPredicates
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.services.PredicateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
    private lateinit var predicateService: PredicateService

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
        literalService.create("Springer")
        literalService.create("ORKG rocks")
        literalService.create("Out of this world")
        literalService.create("We are crazy")

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
        fieldWithPath("extras").description("A dictionary with on-the-fly classes and their corresponding numbers").optional(),
    )
}
