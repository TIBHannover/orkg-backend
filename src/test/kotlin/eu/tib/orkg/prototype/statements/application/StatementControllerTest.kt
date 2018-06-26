package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryStatementRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("Statement Controller")
class StatementControllerTest : RestDocumentationBaseTest() {

    private val repository: StatementRepository =
        InMemoryStatementRepository()

    override fun createController() = StatementController(repository)

    @Test
    fun index() {
        repository.add(
            Statement(
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                ResourceId("123"),
                PredicateId("P432"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun lookupBySubject() {
        repository.add(
            Statement(
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                ResourceId("123"),
                PredicateId("P432"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/subject/123")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }

    @Test
    fun lookupByPredicate() {
        repository.add(
            Statement(
                ResourceId("123"),
                PredicateId("P576"),
                Object.Resource(ResourceId("789"))
            )
        )
        repository.add(
            Statement(
                ResourceId("345"),
                PredicateId("P576"),
                Object.Resource(ResourceId("633"))
            )
        )

        mockMvc
            .perform(
                get("/api/statements/predicate/P576")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].subject").description(
                            "The resource ID"
                        ),
                        fieldWithPath("[].predicate").description(
                            "The predicate ID"
                        ),
                        subsectionWithPath("[].object").description(
                            "The type of object"
                        )
                    )
                )
            )
    }
}
