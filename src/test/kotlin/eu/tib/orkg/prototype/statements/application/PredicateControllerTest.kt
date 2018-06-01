package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document

@DisplayName("Predicate Controller")
class PredicateControllerTest : RestDocumentationBaseTest() {

    @MockBean
    private lateinit var repository: PredicateRepository

    override fun createController() = PredicateController(repository)

    @Test
    fun index() {
        given(repository.findAll()).willReturn(
            listOf(
                Predicate(PredicateId("P123"), "has name"),
                Predicate(PredicateId("P987"), "knows")
            )
        )

        getRequestTo("/api/statements/predicates/")
            .andDo(
                document("predicates")
            )
    }
}
