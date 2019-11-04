package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Class Controller")
@Transactional
class ClassControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: ClassController

    @Autowired
    private lateinit var service: ClassService

    @Autowired
    private lateinit var resourceService: ResourceService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("research contribution")
        service.create("programming language")

        mockMvc
            .perform(getRequestTo("/api/classes/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classListResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("research contribution").id

        mockMvc
            .perform(getRequestTo("/api/classes/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classResponseFields()
                )
            )
    }

    @Test
    fun add() {
        val `class` = mapOf("label" to "foo")

        mockMvc
            .perform(postRequestWithBody("/api/classes/", `class`))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The class label")
                    ),
                    createdResponseHeaders(),
                    classResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.create("research contribution")
        service.create("programming language")
        service.create("research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListResponseFields()
                )
            )
    }

    @Test
    fun lookupByClass() {
        val id = service.create("research contribution").id
        val set = listOf(id).toSet()
        resourceService.create(CreateResourceRequest(null, "Contribution 1", set))
        resourceService.create(CreateResourceRequest(null, "Contribution 2", set))

        mockMvc
            .perform(getRequestTo("/api/classes/$id/resources/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    resourceListResponseFields()
                )
            )
    }

    private fun classResponseFields() =
        responseFields(
            fieldWithPath("id").description("The class ID").optional(),
            fieldWithPath("label").description("The class label"),
            fieldWithPath("uri").description("An optional URI to describe the class (RDF)").optional()
        )

    private fun classListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The class ID").optional(),
            fieldWithPath("[].label").description("The class label"),
            fieldWithPath("[].uri").description("An optional URI to describe the class (RDF)").optional()
        )

    private fun resourceListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The resource ID"),
            fieldWithPath("[].label").description("The resource label"),
            fieldWithPath("[].classes").description("The list of classes the resource belongs to"),
            fieldWithPath("[].created_at").description("The resource creation datetime")
        )
}
