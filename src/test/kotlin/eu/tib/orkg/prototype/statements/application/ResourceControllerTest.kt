package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryResourceRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("Resource Controller")
class ResourceControllerTest : RestDocumentationBaseTest() {

    // FIXME: mock repository, to be independent of in-memory implementation
    private val repository: ResourceRepository =
        InMemoryResourceRepository()

    override fun createController() = ResourceController(repository)

    @Test
    fun index() {
        repository.add(Resource(ResourceId("1"), "knows"))
        repository.add(Resource(ResourceId("2"), "is a"))

        mockMvc
            .perform(
                get("/api/statements/resources/")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].id").description("The resource ID"),
                        fieldWithPath("[].label").description("The resource label")
                    )
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(
                post("/api/statements/resources/")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resource))
            )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The resource label")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The resource ID"),
                        fieldWithPath("label").description("The resource label")
                    )
                )
            )
    }
}
