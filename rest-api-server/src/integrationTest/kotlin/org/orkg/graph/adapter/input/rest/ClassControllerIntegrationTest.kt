package org.orkg.graph.adapter.input.rest

import java.net.URI
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClass
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.testing.fixtures.classResponseFields
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Class Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ClassControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: ClassUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.removeAll()
        resourceService.removeAll()

        assertThat(service.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun index() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language")

        mockMvc
            .perform(getRequestTo("/api/classes/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.createClass(label = "research contribution")

        mockMvc
            .perform(getRequestTo("/api/classes/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(classResponseFields())
                )
            )
    }

    @Test
    fun fetchByURI() {
        // Arrange
        val id = "dummy"
        val label = "dummy label"
        val uri = URI.create("http://example.org/exists")
        service.createClass(id = id, label = label, uri = uri)

        // Act and Assert
        mockMvc
            .perform(getRequestTo("/api/classes/?uri=http://example.org/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.label").value(label))
            .andExpect(jsonPath("$.uri").value(uri.toString()))
            .andDo(
                document(
                    snippet,
                    responseFields(classResponseFields())
                )
            )
    }

    @Test
    fun lookupByIds() {
        val id1 = service.createClass(label = "class1")
        val id2 = service.createClass(label = "class2")

        mockMvc
            .perform(getRequestTo("/api/classes/?ids=$id1,$id2"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language")
        service.createClass(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language (PL)")
        service.createClass(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=PL)"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookupByClass() {
        val id = service.createClass(label = "research contribution")
        val classes = setOf(id.value)
        resourceService.createResource(
            classes = classes,
            label = "Contribution 1"
        )
        resourceService.createResource(
            classes = classes,
            label = "Contribution 2"
        )

        mockMvc
            .perform(getRequestTo("/api/classes/$id/resources/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    resourceListDetailedResponseFields()
                )
            )
    }

    @Test
    @Disabled("This test reproduces the problem, but is easy to fix at the moment. We also changes the domain rules for labels, see #347.")
    fun lookupResourcesForClass() {
        // Given several research problems with the same name
        val classId = service.createClass(label = "research problem")
        val classes = setOf(classId.value)
        val resources = mutableListOf<ThingId>()
        // The regular resource
        resources += resourceService.createResource(
            classes = classes,
            label = "Testing the Darwin's naturalisation hypothesis in invasion biology"
        )
        repeat(5) {
            resources += resourceService.createResource(
                // Other resources, but containing line breaks
                classes = classes,
                label = "Testing the Darwin's naturalisation hypothesis in invasion biology\n"
            )
        }
        val expectedIds = resources.map(ThingId::value).reversed().toJSONArray()

        // When queried, should return all of them
        val query = "Testing the Darwin"
        mockMvc
            .perform(getRequestTo("/api/classes/$classId/resources/?desc=true&exact=false&page=0&sort=id,desc&q=$query"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content..id").value(expectedIds))
    }

    internal fun List<String>.toJSONArray(): JSONArray = JSONArray().apply { addAll(this@toJSONArray) }

    @Test
    fun lookupByClassAndLabel() {
        val id = service.createClass(label = "research contribution")
        val classes = setOf(id.value)
        resourceService.createResource(classes, label = "Math Contribution 1")
        resourceService.createResource(classes, label = "Physics Contribution 1")
        resourceService.createResource(classes, label = "Math Contribution 2")

        mockMvc
            .perform(getRequestTo("/api/classes/$id/resources/?q=Math"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    resourceListDetailedResponseFields()
                )
            )
    }

    private fun resourceListDetailedResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix(
                "content[].", resourceListInnerResponseFields()
            ).andWithPrefix("")

    fun resourceListInnerResponseFields() = listOf(
        fieldWithPath("id").description("The resource ID"),
        fieldWithPath("label").description("The resource label"),
        fieldWithPath("created_at").description("The resource creation datetime"),
        fieldWithPath("created_by").description("The ID of the user that created the resource. All zeros if unknown."),
        fieldWithPath("classes").description("The list of classes the resource belongs to"),
        fieldWithPath("observatory_id").description("The ID of the observatory that maintains this resource."),
        fieldWithPath("extraction_method").description("""Method to extract this resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
        fieldWithPath("organization_id").description("The ID of the organization that maintains this resource."),
        fieldWithPath("shared").description("The number of times this resource is shared"),
        fieldWithPath("formatted_label").description("The formatted label of the resource if available").optional(),
        fieldWithPath("_class").description("Resource").optional(),
        fieldWithPath("visibility").description("Visibility").optional().ignored(),
        fieldWithPath("verified").description("Verified").optional().ignored(),
        fieldWithPath("featured").description("Featured").optional().ignored(),
        fieldWithPath("unlisted").description("Unlisted").optional().ignored(),
        fieldWithPath("modifiable").description("Whether this resource can be modified.").optional().ignored(),
    )

    fun classListDetailedResponseFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", classResponseFields()
        ).andWithPrefix("")
}
