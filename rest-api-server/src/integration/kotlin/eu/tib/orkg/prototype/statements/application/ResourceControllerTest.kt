package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.LiteralService
import eu.tib.orkg.prototype.statements.services.PaperService
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assumptions.assumeTrue
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Resource Controller")
@Transactional
@Import(MockUserDetailsService::class)
@TestPropertySource(properties = ["orkg.features.formatted_labels=false"])
class ResourceControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var service: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var paperService: PaperService

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var literalService: LiteralService

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.removeAll()
        classService.removeAll()
        predicateService.removeAll()
        statementService.removeAll()
        literalService.removeAll()

        assertThat(service.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(literalService.findAll(tempPageable)).hasSize(0)
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addButClassDoesNotExist() {
        val resource = mapOf("label" to "foo", "classes" to setOf(ClassId("doesNotExist")))

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isBadRequest)
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
        val oldClass = classService.create("class")
        val resource = service.create(CreateResourceRequest(null, "foo", setOf(oldClass.id))).id

        val newLabel = "bar"
        val update = mapOf("label" to newLabel, "classes" to listOf(oldClass.id.value))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The updated resource label"),
                        fieldWithPath("classes").description("The classes to which the resource belongs to").optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    fun editResourceClass() {
        val oldClass = classService.create("class")
        val resource = service.create(CreateResourceRequest(null, "test", setOf(oldClass.id))).id

        val newClass = classService.create("clazz")
        val update = mapOf("classes" to listOf(newClass.id.value))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("test"))
            .andExpect(jsonPath("$.classes[0]").value(newClass.id.value))
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
    fun editResourceClassesIsEmpty() {
        val oldClass = classService.create("class")
        val resource = service.create(CreateResourceRequest(null, "test", setOf(oldClass.id))).id

        val update = mapOf("classes" to emptyList<ClassId>())

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun excludeByClass() {
        val id = classService.create("research contribution").id
        val set = setOf(id)
        service.create(CreateResourceRequest(null, "Contribution 1", set))
        service.create(CreateResourceRequest(null, "Contribution 2"))
        service.create(CreateResourceRequest(null, "Contribution 3"))
        val id2 = classService.create("research contribution").id
        val set2 = setOf(id2)
        service.create(CreateResourceRequest(null, "Paper Contribution 1", set2))

        mockMvc
            .perform(
                getRequestTo("/api/resources/?q=Contribution&exclude=$id,$id2")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude").description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional(),
                        parameterWithName("include").description("List of classes to include e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun includeByClass() {
        val id = classService.create("research contribution").id
        val set = setOf(id)
        service.create(CreateResourceRequest(null, "Contribution 1", set))
        service.create(CreateResourceRequest(null, "Contribution 2"))
        service.create(CreateResourceRequest(null, "Contribution 3"))
        val id2 = classService.create("research contribution").id
        val set2 = setOf(id2)
        service.create(CreateResourceRequest(null, "Paper Contribution 1", set2))

        mockMvc
            .perform(
                getRequestTo("/api/resources/?q=Contribution&include=$id")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label"),
                        parameterWithName("exact").description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude").description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional(),
                        parameterWithName("include").description("List of classes to include e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun includeAndExcludeByClassError() {
        mockMvc
            .perform(
                getRequestTo("/api/resources/?q=Contribution&include=Error,NoError&exclude=Error")
            )
            .andExpect(status().isBadRequest)
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
        val id = service.create("bye bye").id

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
        val id = service.create("parent").id
        val obj = service.create("son").id
        val rel = predicateService.create("related").id
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
        val id = service.create("To Delete").id

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
        val id = classService.create("Class 1").id
        val set = setOf(id)
        service.create(CreateResourceRequest(null, "Resource 1", set))
        service.create(CreateResourceRequest(null, "Resource 2", set))

        val resId = service.create(CreateResourceRequest(null, "Resource 3")).id
        val con1 = service.create(CreateResourceRequest(null, "Connection 1")).id
        val con2 = service.create(CreateResourceRequest(null, "Connection 2")).id
        val pred = predicateService.create("Test predicate").id
        statementService.create(con1.value, pred, resId.value)
        statementService.create(con2.value, pred, resId.value)
        val id2 = classService.create("Class 2").id
        val set2 = setOf(id2)
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

    @Test
    fun testPaperContributorsDetails() {
        predicateService.create(CreatePredicateRequest(PredicateId("P26"), "Has DOI"))
        predicateService.create(CreatePredicateRequest(PredicateId("P29"), "Has publication year"))
        predicateService.create(CreatePredicateRequest(PredicateId("P30"), "Has Research field"))
        predicateService.create(CreatePredicateRequest(PredicateId("P31"), "Has contribution"))
        predicateService.create(CreatePredicateRequest(PredicateId("P32"), "Has research problem"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_EVALUATION"), "Has evaluation"))

        resourceService.create(CreateResourceRequest(ResourceId("R3003"), "Question Answering over Linked Data"))

        classService.create(CreateClassRequest(ClassId("Paper"), "paper", null))
        classService.create(CreateClassRequest(ClassId("Contribution"), "Contribution", null))
        classService.create(CreateClassRequest(ClassId("Problem"), "Problem", null))

        val userId = createTestUser()
        // create resource with different userId, and use it as a research field in the paper
        resourceService.create(userId, CreateResourceRequest(ResourceId("R20"), "database"), ObservatoryId.createUnknownObservatory(), ExtractionMethod.UNKNOWN, OrganizationId.createUnknownOrganization())
        val originalPaper = PaperControllerTest().createDummyPaperObject(researchField = "R20")
        val paperId = paperService.addPaperContent(originalPaper, true, UUID.randomUUID()).id.value
        val result = mockMvc
            .perform(getRequestTo("/api/resources/$paperId/contributors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andReturn()

        val contributors = objectMapper.readValue<List<ResourceContributors>>(
            result.response.contentAsString,
            objectMapper.typeFactory.constructCollectionType(MutableList::class.java, ResourceContributors::class.java)
        )
        // research field creator should not be in paper contributors list
        assertThat(contributors[0].createdBy).isNotEqualTo(userId.value.toString())
        // must have distinct set i.e, one contributor shouldn't appear more than once for same date
        // distinct and original list must have same size
        assertThat(contributors.mapTo(mutableListOf()) { listOf(it.createdAt, it.createdBy) }.distinct().size).isEqualTo(contributors.size)
    }

    @Test
    fun `fetch resource with the correct formatted label`() {
        assumeTrue(flags.isFormattedLabelsEnabled())
        val value = "Wow!"
        val id = createTemplateAndTypedResource(value)

        mockMvc
            .perform(getRequestTo("/api/resources/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.formatted_label").value("xx${value}xx"))
            .andDo(
                document(
                    snippet,
                    responseFields(resourceResponseFields())
                )
            )
    }

    fun createTemplateAndTypedResource(value: String): ResourceId {
        // create required classes and predicates
        val templateClass = classService.create(
            CreateClassRequest(
                ClassId("ContributionTemplate"),
                "Contribution Template",
                null
            )
        )
        val throwAwayClass = classService.create("Templated Class")
        val templateLabelPredicate = predicateService.create(
            CreatePredicateRequest(
                PredicateId("TemplateLabelFormat"),
                "Template label format"
            )
        )
        val templateClassPredicate = predicateService.create(
            CreatePredicateRequest(
                PredicateId("TemplateOfClass"),
                "Template of class"
            )
        )
        val templateComponentPredicate = predicateService.create(
            CreatePredicateRequest(
                PredicateId("TemplateComponent"),
                "Template component"
            )
        )
        val templateComponentClass = classService.create(
            CreateClassRequest(
                ClassId("TemplateComponentClass"),
                "Template component class",
                null
            )
        )
        val throwAwayProperty = predicateService.create("Temp property")
        val templateComponentPropertyPredicate = predicateService.create(
            CreatePredicateRequest(
                PredicateId("TemplateComponentProperty"),
                "Template component property"
            )
        )
        // create the template
        val template = service.create(
            CreateResourceRequest(
                null,
                "Throw-way template",
                setOf(ClassId(templateClass.id.value))
            )
        )
        val labelFormat = literalService.create("xx{${throwAwayProperty.id}}xx")
        statementService.create(template.id.value, templateLabelPredicate.id, labelFormat.id.value)
        statementService.create(template.id.value, templateClassPredicate.id, throwAwayClass.id.value)
        val templateComponent = service.create(
            CreateResourceRequest(
                null,
                "component 1",
                setOf(ClassId(templateComponentClass.id.value))
            )
        )
        statementService.create(template.id.value, templateComponentPredicate.id, templateComponent.id.value)
        statementService.create(templateComponent.id.value, templateComponentPropertyPredicate.id, throwAwayProperty.id.value)
        // Create resource and type it
        val templatedResource = service.create(
            CreateResourceRequest(
                null,
                "Fancy resource",
                setOf(ClassId(throwAwayClass.id.value))
            )
        )
        val someValue = literalService.create(value)
        statementService.create(templatedResource.id.value, throwAwayProperty.id, someValue.id.value)
        return templatedResource.id
    }

    fun listOfDetailedResourcesResponseFields(): ResponseFieldsSnippet {
        return responseFields(pageableDetailedFieldParameters())
            .andWithPrefix(
                "content[].", resourceResponseFields()
            ).andWithPrefix("")
    }

    fun createTestUser(): ContributorId {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return ContributorId(userService.findByEmail("abc@gmail.com").get().id!!)
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
            fieldWithPath("_class").description("Class").optional(),
            fieldWithPath("verified").description("Determines if the resource was verified by a curator.").optional()
                .ignored(),
            fieldWithPath("featured").description("Featured Value").optional().ignored(),
            fieldWithPath("unlisted").description("Unlisted Value").optional().ignored(),
            fieldWithPath("formatted_label").description("The formatted label of the resource if available").optional()
        )

        fun listOfResourcesResponseFields(): ResponseFieldsSnippet =
            responseFields(
                fieldWithPath("[]").description("A list of resources")
            )
                .andWithPrefix("[].", resourceResponseFields())
                .andWithPrefix("")
    }
}
