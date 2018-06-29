package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import eu.tib.orkg.prototype.statements.infrastructure.InMemoryResourceRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
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
        repository.add(Resource(ResourceId("1"), "research contribution"))
        repository.add(Resource(ResourceId("2"), "programming language"))

        mockMvc
            .perform(
                get("/api/resources/")
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
    fun lookup() {
        repository.add(Resource(ResourceId("1"), "research contribution"))
        repository.add(Resource(ResourceId("2"), "programming language"))
        repository.add(Resource(ResourceId("3"), "research topic"))

        mockMvc
            .perform(
                get("/api/resources/?q=research")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("The resource ID"),
                        fieldWithPath("[].label").description("The resource label")
                    )
                )
            )
    }

    @Test
    fun fetch() {
        repository.add(Resource(ResourceId("1"), "research contribution"))

        mockMvc
            .perform(
                get("/api/resources/1")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("id").description("The resource ID"),
                        fieldWithPath("label").description("The resource label")
                    )
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(
                post("/api/resources/")
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
                    responseHeaders(
                        headerWithName("Location").description("Location to the created resource")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The resource ID"),
                        fieldWithPath("label").description("The resource label")
                    )
                )
            )
    }
}
