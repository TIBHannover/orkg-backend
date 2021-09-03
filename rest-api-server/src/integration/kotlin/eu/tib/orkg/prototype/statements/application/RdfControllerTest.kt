package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("RDF Controller")
@Transactional
class RdfControllerTest(neo4jClient: Neo4jClient) : RestDocumentationBaseTest(neo4jClient) {

    @Autowired
    private lateinit var service: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var classService: ClassService

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.removeAll()
        predicateService.removeAll()
        classService.removeAll()

        assertThat(service.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun index() {
        service.create("Resource 1")
        service.create("Resource 2")
        service.create("Resource 3")

        mockMvc
            .perform(getFileRequestTo("/api/rdf/dump"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Resource 1")))
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun testFilterResources() {
        service.create("Resource 1")
        service.create("Resource 2")
        service.create("Resource 3")

        mockMvc
            .perform(getRequestTo("/api/rdf/hints?q=1&type=item"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Resource 1")))
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun testFilterPredicate() {
        predicateService.create("Predicate XX")
        predicateService.create("Predicate YY")
        predicateService.create("Predicate ZZ")

        mockMvc
            .perform(getRequestTo("/api/rdf/hints?q=XX&type=property"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Predicate XX")))
            .andExpect(content().string(not(containsString("Resource"))))
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun testFilterClass() {
        classService.create("Class *")
        classService.create("Class +")
        classService.create("Class /")

        mockMvc
            .perform(getRequestTo("/api/rdf/hints?q=/&type=class"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Class /")))
            .andExpect(content().string(not(containsString("Resource"))))
            .andExpect(content().string(not(containsString("Predicate"))))
            .andDo(
                document(
                    snippet
                )
            )
    }
}
