package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document

@DisplayName("Resource Controller")
class ResourceControllerTest : RestDocumentationBaseTest() {

    @MockBean
    private lateinit var repository: ResourceRepository

    override fun createController() = ResourceController(repository)

    @Test
    fun index() {
        getRequestTo("/api/statements/resources/")
            .andDo(
                document("resources")
            )
    }
}
