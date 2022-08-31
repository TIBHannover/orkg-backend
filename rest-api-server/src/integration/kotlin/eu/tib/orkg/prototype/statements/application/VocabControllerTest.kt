package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.services.PredicateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Vocab Controller")
@Transactional
class VocabControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        classService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun resource() {
        val id = resourceService.create("Resource 1").id

        mockMvc
            .perform(getRequestTo("/api/vocab/resource/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun predicate() {
        val id = predicateService.create("Predicate 1").id

        mockMvc
            .perform(getRequestTo("/api/vocab/predicate/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun clazz() {
        val id = classService.create("Class 1").id

        mockMvc
            .perform(getRequestTo("/api/vocab/class/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }
}
