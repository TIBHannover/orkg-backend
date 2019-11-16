package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.application.rdf.VocabController
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Vocab Controller")
@Transactional
class VocabControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: VocabController

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    fun resource() {
        val id = resourceService.create("Resource 1").id!!

        mockMvc
            .perform(getRequestTo("/vocab/resource/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun predicate() {
        val id = predicateService.create("Predicate 1").id!!

        mockMvc
            .perform(getRequestTo("/vocab/predicate/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun clazz() {
        val id = classService.create("Class 1").id!!

        mockMvc
            .perform(getRequestTo("/vocab/class/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }
}
