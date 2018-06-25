package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryResourceRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("Resource Controller")
class ResourceControllerTest : RestDocumentationBaseTest() {

    @MockBean
    private lateinit var repository: ResourceRepository

    // FIXME: mock repository, to be independent of in-memory implementation
    override fun createController() =
        ResourceController(InMemoryResourceRepository())

    @Test
    fun index() {
        getRequestTo("/api/statements/resources/")
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
