package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Resource Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ResourceControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: ResourceController

    @Autowired
    private lateinit var service: ResourceService

    @Autowired
    private lateinit var classService: ClassService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var statementService: StatementService

    override fun createController() = controller

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.removeAll()
        classService.removeAll()
        predicateService.removeAll()
        statementService.removeAll()

        Assertions.assertThat(service.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(classService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(statementService.findAll(tempPageable)).hasSize(0)
    }

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
                    pageableRequestParameters(),
                    listOfDetailedResourcesResponseFields()
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
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.create("research contribution")
        service.create("programming language (PL)")
        service.create("research topic")

        mockMvc
            .perform(getRequestTo("/api/resources/?q=PL)"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
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
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
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
                    responseFields(resourceResponseFields())
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
                    responseFields(resourceResponseFields())
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
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    fun excludeByClass() {
        val id = classService.create("research contribution").id!!
        val set = listOf(id).toSet()
        service.create(CreateResourceRequest(null, "Contribution 1", set))
        service.create(CreateResourceRequest(null, "Contribution 2"))
        service.create(CreateResourceRequest(null, "Contribution 3"))
        val id2 = classService.create("research contribution").id!!
        val set2 = listOf(id2).toSet()
        service.create(CreateResourceRequest(null, "Paper Contribution 1", set2))

        mockMvc
            .perform(
                getRequestTo("/api/resources/?q=Contribution&exclude=$id,$id2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude").description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                        ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deleteResourceNotFound() {
        mockMvc
            .perform(deleteRequest("/api/resources/NONEXISTENT"))
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deleteResourceSuccess() {
        val id = service.create("bye bye").id!!

        mockMvc
            .perform(deleteRequest("/api/resources/$id"))
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @WithUserDetails("admin", userDetailsServiceBeanName = "mockUserDetailsService")
    fun deleteResourceForbidden() {
        val id = service.create("parent").id!!
        val obj = service.create("son").id!!
        val rel = predicateService.create("related").id!!
        statementService.create(id.value, rel, obj.value)

        mockMvc
            .perform(deleteRequest("/api/resources/$id"))
            .andExpect(status().isForbidden)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    @Disabled("throwing an exception with the message (An Authentication object was not found in the SecurityContext)")
    fun deleteResourceWithoutLogin() {
        val id = service.create("To Delete").id!!

        mockMvc
            .perform(deleteRequest("/api/resources/$id"))
            .andExpect(status().isUnauthorized)
            .andDo(
                document(
                    snippet
                )
            )
    }

    @Test
    fun testSharedIndicatorWhenResourcesWithClassExclusion() {
        val id = classService.create("Class 1").id!!
        val set = listOf(id).toSet()
        service.create(CreateResourceRequest(null, "Resource 1", set))
        service.create(CreateResourceRequest(null, "Resource 2", set))

        val resId = service.create(CreateResourceRequest(null, "Resource 3")).id!!
        val con1 = service.create(CreateResourceRequest(null, "Connection 1")).id!!
        val con2 = service.create(CreateResourceRequest(null, "Connection 2")).id!!
        val pred = predicateService.create("Test predicate").id!!
        statementService.create(con1.value, pred, resId.value)
        statementService.create(con2.value, pred, resId.value)
        val id2 = classService.create("Class 2").id!!
        val set2 = listOf(id2).toSet()
        service.create(CreateResourceRequest(null, "Another Resource", set2))

        mockMvc
            .perform(getRequestTo("/api/resources/?q=Resource&exclude=$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[?(@.label == 'Resource 3')].shared").value(2))
            .andExpect(jsonPath("$.content[?(@.label == 'Another Resource')].shared").value(0))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                            .optional(),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude").description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    fun listOfDetailedResourcesResponseFields(): ResponseFieldsSnippet {
        return responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", resourceResponseFields()
        ).andWithPrefix("")
    }

    companion object RestDoc {
        fun resourceResponseFields() = listOf(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label"),
            fieldWithPath("created_at").description("The resource creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the resource. All zeros if unknown."),
            fieldWithPath("classes").description("The list of classes the resource belongs to"),
            fieldWithPath("observatory_id").description("The ID of the observatory that maintains this resource."),
            fieldWithPath("extraction_method").description("""Method to extract this resource. Can be one of "unknown", "manual" or "automatic"."""),
            fieldWithPath("organization_id").description("The ID of the organization that maintains this resource."),
            fieldWithPath("shared").description("The number of times this resource is shared").optional(),
            fieldWithPath("_class").description("Class").optional()
        )

        fun listOfResourcesResponseFields(): ResponseFieldsSnippet =
            responseFields(
                fieldWithPath("[]").description("A list of resources"))
                .andWithPrefix("[].", resourceResponseFields())
                .andWithPrefix("")
    }
}
