package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryPredicateRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("Predicate Controller")
class PredicateControllerTest : RestDocumentationBaseTest() {

    private val repository: PredicateRepository =
        InMemoryPredicateRepository()

    override fun createController() = PredicateController(repository)

    @Test
    fun index() {
        repository.add(Predicate(PredicateId("P123"), "has name"))
        repository.add(Predicate(PredicateId("P987"), "knows"))

        mockMvc
            .perform(
                get("/api/statements/predicates/")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].id").description("The predicate ID"),
                        fieldWithPath("[].label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun fetch() {
        repository.add(Predicate(PredicateId("P123"), "has name"))

        mockMvc
            .perform(
                get("/api/statements/predicates/P123")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("id").description("The predicate ID"),
                        fieldWithPath("label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun lookup() {
        repository.add(Predicate(PredicateId("P123"), "has name"))
        repository.add(Predicate(PredicateId("P345"), "gave name to"))
        repository.add(Predicate(PredicateId("P987"), "knows"))

        mockMvc
            .perform(
                get("/api/statements/predicates/?q=name")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("The predicate ID"),
                        fieldWithPath("[].label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "knows")

        mockMvc
            .perform(
                post("/api/statements/predicates/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resource))
            )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The predicate label")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The predicate ID"),
                        fieldWithPath("label").description("The predicate label")
                    )
                )
            )
    }
}
