package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Class Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ClassControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: ClassController

    @Autowired
    private lateinit var service: ClassService

    @Autowired
    private lateinit var resourceService: ResourceService

    override fun createController() = controller

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
        service.create("research contribution")
        service.create("programming language")

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
    fun fetchByURI() {
        // Arrange
        service.create(CreateClassRequest(ClassId("dummy"), "dummy label", URI.create("http://example.org/exists")))

        // Act and Assert
        mockMvc
            .perform(getRequestTo("/api/classes/?uri=http://example.org/exists"))
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(jsonPath("\$.id").value("dummy"))
            .andExpect(jsonPath("\$.label").value("dummy label"))
            .andExpect(jsonPath("\$.uri").value("http://example.org/exists"))
            .andDo(
                document(
                    snippet,
                    classResponseFields()
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun add() {
        val `class` = mapOf("label" to "foo", "uri" to "http://example.org/bar")

        mockMvc
            .perform(postRequestWithBody("/api/classes/", `class`))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.label").value("foo"))
            .andExpect(jsonPath("$.uri").value("http://example.org/bar"))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The class label"),
                        fieldWithPath("uri").description("The class URI")
                    ),
                    createdResponseHeaders(),
                    classResponseFields()
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addExistingId() {
        service.create(CreateClassRequest(id = ClassId("dummy"), label = "foo", uri = null))
        val duplicateClass = mapOf("label" to "bar", "id" to "dummy")

        mockMvc
            .perform(postRequestWithBody("/api/classes/", duplicateClass))
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("id").description("The class id"),
                        fieldWithPath("label").description("The class label")
                    )
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addExistingURI() {
        service.create(
            CreateClassRequest(
                id = ClassId("some-id"),
                label = "foo",
                uri = URI.create("http://example.org/in-use")
            )
        )
        val duplicateClass = mapOf(
            "label" to "bar",
            "uri" to "http://example.org/in-use"
        )

        mockMvc
            .perform(postRequestWithBody("/api/classes/", duplicateClass))
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The class label"),
                        fieldWithPath("uri").description("The URI of the class")
                    )
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addReservedId() {
        val reservedClass = mapOf("label" to "bar", "id" to "Resource")

        mockMvc
            .perform(postRequestWithBody("/api/classes/", reservedClass))
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("id").description("The class id"),
                        fieldWithPath("label").description("The class label")
                    )
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
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.create("research contribution")
        service.create("programming language (PL)")
        service.create("research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=PL)"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    RequestDocumentation.requestParameters(
                        RequestDocumentation.parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookupByClass() {
        val id = service.create("research contribution").id!!
        val set = listOf(id).toSet()
        resourceService.create(CreateResourceRequest(null, "Contribution 1", set))
        resourceService.create(CreateResourceRequest(null, "Contribution 2", set))

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
    fun edit() {
        val `class` = service.create(CreateClassRequest(id = null, label = "foo", uri = URI("http://example.org/foo"))).id!!

        val newLabel = "bar"
        // Set properties that are not supposed to be updated to "null" to prevent regressions.
        // Make sure to add an assertion for those as well.
        val resource = mapOf("label" to newLabel, "uri" to null)

        mockMvc
            .perform(putRequestWithBody("/api/classes/$`class`", resource))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andExpect(jsonPath("$.uri").value("http://example.org/foo")) // old value
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The updated class label"),
                        fieldWithPath("uri").ignored()
                    ), classResponseFields()
                )
            )
    }

    @Test
    fun lookupByClassAndLabel() {
        val id = service.create("research contribution").id!!
        val set = listOf(id).toSet()
        resourceService.create(CreateResourceRequest(null, "Math Contribution 1", set))
        resourceService.create(CreateResourceRequest(null, "Physics Contribution1", set))
        resourceService.create(CreateResourceRequest(null, "Math Contribution 2", set))

        mockMvc
            .perform(getRequestTo("/api/classes/$id/resources/?q=Math"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    resourceListDetailedResponseFields()
                )
            )
    }

    private fun classResponseFields() =
        responseFields(
            fieldWithPath("id").description("The class ID").optional(),
            fieldWithPath("label").description("The class label"),
            fieldWithPath("uri").description("An optional URI to describe the class (RDF)").optional(),
            fieldWithPath("created_at").description("The class creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the class. All zeros if unknown."),
            fieldWithPath("description").description("The description of the class, if exists.").optional(),
            fieldWithPath("_class").optional().ignored(),
            fieldWithPath("featured").description("Featured Value").optional().ignored(),
            fieldWithPath("unlisted").description("Unlisted Value").optional().ignored()
        )

    private fun classListResponseFields() =
        listOf(
            fieldWithPath("id").description("The class ID").optional(),
            fieldWithPath("label").description("The class label"),
            fieldWithPath("uri").description("An optional URI to describe the class (RDF)").optional(),
            fieldWithPath("created_at").description("The class creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the class. All zeros if unknown."),
            fieldWithPath("description").description("The description of the class, if exists.").optional(),
            fieldWithPath("_class").optional().ignored(),
            fieldWithPath("featured").description("Featured Value").optional().ignored(),
            fieldWithPath("unlisted").description("Unlisted Value").optional().ignored()
        )

    private fun resourceListDetailedResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", resourceListInnerResponseFields()
                    ).andWithPrefix("")

    fun resourceListInnerResponseFields() = listOf(
        fieldWithPath("id").description("The resource ID"),
        fieldWithPath("label").description("The resource label"),
        fieldWithPath("created_at").description("The resource creation datetime"),
        fieldWithPath("created_by").description("The ID of the user that created the resource. All zeros if unknown."),
        fieldWithPath("classes").description("The list of classes the resource belongs to"),
        fieldWithPath("observatory_id").description("The ID of the observatory that maintains this resource."),
        fieldWithPath("extraction_method").description("""Method to extract this resource. Can be one of "unknown", "manual" or "automatic"."""),
        fieldWithPath("organization_id").description("The ID of the organization that maintains this resource."),
        fieldWithPath("shared").description("The number of times this resource is shared"),
        fieldWithPath("_class").description("Resource").optional(),
        fieldWithPath("featured").description("Featured").optional().ignored(),
        fieldWithPath("unlisted").description("Unlisted").optional().ignored()
    )

    fun classListDetailedResponseFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", classListResponseFields()
        ).andWithPrefix("")
}
