package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.application.rdf.RdfController
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("RDF Controller")
@Transactional
class RdfControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: RdfController

    @Autowired
    private lateinit var service: ResourceService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("Resource 1")
        service.create("Resource 2")
        service.create("Resource 3")

        mockMvc
            .perform(getFileRequestTo("/api/dump/rdf"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Resource 1")))
            .andDo(
                document(
                    snippet
                )
            )
    }
}
