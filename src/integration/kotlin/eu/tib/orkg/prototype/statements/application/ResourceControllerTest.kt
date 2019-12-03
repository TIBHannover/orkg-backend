package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Resource Controller")
@Transactional
class ResourceControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: ResourceController

    @Autowired
    private lateinit var service: ResourceService

    @Autowired
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("research contribution")
        service.create("programming language")

        mockMvc
            .perform(getRequestTo("/api/resources/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional()
                    ),
                    resourceListResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.create("research contribution")
        service.create("programming language")
        service.create("research topic")

        mockMvc
            .perform(getRequestTo("/api/resources/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment").optional()
                    ),
                    resourceListResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("research contribution").id

        mockMvc
            .perform(getRequestTo("/api/resources/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    resourceResponseFields()
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The resource label")
                    ),
                    createdResponseHeaders(),
                    resourceResponseFields()
                )
            )
    }

    @Test
    fun addWithExistingIds() {
        val resource = mapOf("label" to "bar", "id" to "Test")

        service.create(CreateResourceRequest(ResourceId("Test"), "foo"))

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun edit() {
        val resource = service.create("foo").id!!

        val newLabel = "bar"
        val update = mapOf("label" to newLabel)

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The updated resource label")
                    ),
                    resourceResponseFields()
                )
            )
    }

    @Test
    fun editResourceClass() {
        classService.create("class")
        val resource = service.create(CreateResourceRequest(null, "test", setOf(ClassId("class")))).id!!

        val newClass = classService.create("clazz")
        val update = mapOf("classes" to listOf(newClass.id!!.value))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("test"))
            .andExpect(jsonPath("$.classes[0]").value(newClass.id!!.value))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").type(String).description("The updated resource label").optional(),
                        fieldWithPath("classes").description("The classes to which the resource belongs to").optional()
                    ),
                    resourceResponseFields()
                )
            )
    }

    @Test
    fun excludeByClass() {
        val id = classService.create("research contribution").id!!
        val set = listOf(id).toSet()
        service.create(CreateResourceRequest(null, "Contribution 1", set))
        service.create(CreateResourceRequest(null, "Contribution 2", set))

        service.create(CreateResourceRequest(null, "Contribution 3"))
        val id2 = classService.create("research contribution").id!!
        val set2 = listOf(id2).toSet()
        service.create(CreateResourceRequest(null, "Paper Contribution 1", set2))

        mockMvc
            .perform(getRequestTo("/api/resources/?q=Contribution&exclude=$id,$id2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment").optional(),
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional(),
                        parameterWithName("exclude").description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)").optional()
                    ),
                    resourceListResponseFields()
                )
            )
    }

    private fun resourceResponseFields() =
        responseFields(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label"),
            fieldWithPath("created_at").description("The resource creation datetime"),
            fieldWithPath("classes").description("The list of classes the resource belongs to"),
            fieldWithPath("shared").description("The number of times this resource is shared").optional()
        )

    private fun resourceListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The resource ID"),
            fieldWithPath("[].label").description("The resource label"),
            fieldWithPath("[].created_at").description("The resource creation datetime"),
            fieldWithPath("[].classes").description("The list of classes the resource belongs to"),
            fieldWithPath("[].shared").description("The number of times this resource is shared").optional()
        )
}
