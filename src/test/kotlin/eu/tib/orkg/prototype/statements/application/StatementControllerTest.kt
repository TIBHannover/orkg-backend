package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document

@DisplayName("Statement Controller")
class StatementControllerTest : RestDocumentationBaseTest() {

    @MockBean
    private lateinit var repository: StatementRepository

    override fun createController() = StatementController(repository)

    @Test
    fun index() {
        getRequestTo("/api/statements/")
            .andDo(
                document("statements")
            )
    }
}
