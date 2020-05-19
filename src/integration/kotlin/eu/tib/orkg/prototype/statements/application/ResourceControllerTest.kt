package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
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
    private lateinit var statementController: StatementService

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
        service.create(CreateResourceRequest(null, "Contribution 2"))
        service.create(CreateResourceRequest(null, "Contribution 3"))
        val id2 = classService.create("research contribution").id!!
        val set2 = listOf(id2).toSet()
        service.create(CreateResourceRequest(null, "Paper Contribution 1", set2))

        mockMvc
            .perform(getRequestTo("/api/resources/?q=Contribution&exclude=$id,$id2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
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
        statementController.create(con1.value, pred, resId.value)
        statementController.create(con2.value, pred, resId.value)
        val id2 = classService.create("Class 2").id!!
        val set2 = listOf(id2).toSet()
        service.create(CreateResourceRequest(null, "Another Resource", set2))

        mockMvc
            .perform(getRequestTo("/api/resources/?q=Resource&exclude=$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].shared").value(2))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label").optional(),
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
            fieldWithPath("created_by").description("The ID of the user that created the resource. All zeros if unknown."),
            fieldWithPath("classes").description("The list of classes the resource belongs to"),
            fieldWithPath("observatory_id").description("The ID of the observatory that maintains this resource."),
            fieldWithPath("extraction_method").description("""Method to extract this resource. Can be one of "unknown", "manual" or "automatic"."""),
            fieldWithPath("shared").description("The number of times this resource is shared").optional(),
            fieldWithPath("_class").optional().ignored()
        )

    private fun resourceListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The resource ID"),
            fieldWithPath("[].label").description("The resource label"),
            fieldWithPath("[].created_at").description("The resource creation datetime"),
            fieldWithPath("[].created_by").description("The ID of the user that created the resource. All zeros if unknown."),
            fieldWithPath("[].classes").description("The list of classes the resource belongs to"),
            fieldWithPath("[].observatory_id").description("The ID of the observatory that maintains this resource."),
            fieldWithPath("[].extraction_method").description("""Method to extract this resource. Can be one of "unknown", "manual" or "automatic"."""),
            fieldWithPath("[].shared").description("The number of times this resource is shared").optional(),
            fieldWithPath("[]._class").optional().ignored()
        )
}
