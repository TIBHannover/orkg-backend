package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.PaperService
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
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var paperService: PaperService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var literalService: LiteralUseCases

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
        service.createResource(label = "research contribution")
        service.createResource(label = "programming language")
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
        service.createResource(label = "research contribution")
        service.createResource(label = "programming language")
        service.createResource(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/resources/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q")
                            .description("A search term that must be contained in the label"),
                        parameterWithName("exact")
                            .description("Whether it is an exact string lookup or just containment")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createResource(label = "research contribution")
        service.createResource(label = "programming language (PL)")
        service.createResource(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/resources/?q=PL)"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q")
                            .description("A search term that must be contained in the label"),
                        parameterWithName("exact")
                            .description("Whether it is an exact string lookup or just containment")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.createResource(label = "research contribution")

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
                        fieldWithPath("label")
                            .description("The resource label")
                    ),
                    createdResponseHeaders(),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun addButClassDoesNotExist() {
        val resource = mapOf(
            "label" to "foo",
            "classes" to setOf(ThingId("doesNotExist"))
        )

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun addWithExistingIds() {
        val resource = mapOf("label" to "bar", "id" to "Test")

        service.createResource(id = "Test", label = "foo")

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun edit() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "foo")
        val newLabel = "bar"
        val update = mapOf("label" to newLabel, "classes" to setOf(oldClass))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label")
                            .description("The updated resource label"),
                        fieldWithPath("classes")
                            .description("The classes to which the resource belongs to")
                            .optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    fun editResourceClass() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

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
                        fieldWithPath("label")
                            .type(String)
                            .description("The updated resource label")
                            .optional(),
                        fieldWithPath("classes")
                            .description("The classes to which the resource belongs to"
                            ).optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    fun editResourceClassesIsEmpty() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to emptyList<ClassId>())

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.classes", hasSize<Int>(0)))
    }

    @Test
    fun editResourceClassesAreInvalid() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to setOf(ThingId("DoesNotExist")))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun excludeByClass() {
        val id = classService.createClass(label = "research contribution")
        service.createResource(classes = setOf(id.value), label = "Contribution 1")
        service.createResource(label = "Contribution 2")
        service.createResource(label = "Contribution 3")
        val id2 = classService.createClass(label = "research contribution")
        service.createResource(classes = setOf(id2.value), label = "Paper Contribution 1")
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
                        parameterWithName("q")
                            .description("A search term that must be contained in the label"),
                        parameterWithName("exact")
                            .description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude")
                            .description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional(),
                        parameterWithName("include")
                            .description("List of classes to include e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun includeByClass() {
        val id = classService.createClass(label = "research contribution")
        service.createResource(classes = setOf(id.value), label = "Contribution 1")
        service.createResource(label = "Contribution 2")
        service.createResource(label = "Contribution 3")
        val id2 = classService.createClass(label = "research contribution")
        service.createResource(classes = setOf(id2.value), label = "Paper Contribution 1")

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
                        parameterWithName("q")
                            .description("A search term that must be contained in the label"),
                        parameterWithName("exact")
                            .description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude")
                            .description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional(),
                        parameterWithName("include")
                            .description("List of classes to include e.g Paper,C0,Contribution (default: not provided)")
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
        val id = service.createResource(label = "bye bye")

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
        val subject = service.createResource(label = "parent")
        val `object` = service.createResource(label = "son")
        val predicate = predicateService.createPredicate(label = "related")
        statementService.create(subject.value, predicate, `object`.value)

        mockMvc
            .perform(deleteRequest("/api/resources/$subject"))
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
        val id = service.createResource(label = "To Delete")

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
        val id = classService.createClass(label = "Class 1")
        val classes = setOf(id.value)
        service.createResource(classes = classes, label = "Resource 1")
        service.createResource(classes = classes, label = "Resource 2")

        val resId = service.createResource(label = "Resource 3")
        val con1 = service.createResource(label = "Connection 1")
        val con2 = service.createResource(label = "Connection 2")
        val predicate = predicateService.createPredicate(label = "Test predicate")
        statementService.create(con1.value, predicate, resId.value)
        statementService.create(con2.value, predicate, resId.value)
        val id2 = classService.createClass(label = "Class 2")
        service.createResource(classes = setOf(id2.value), label = "Another Resource")

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
                        parameterWithName("q")
                            .description("A search term that must be contained in the label")
                            .optional(),
                        parameterWithName("exact")
                            .description("Whether it is an exact string lookup or just containment")
                            .optional(),
                        parameterWithName("exclude")
                            .description("List of classes to exclude e.g Paper,C0,Contribution (default: not provided)")
                            .optional()
                    ),
                    listOfDetailedResourcesResponseFields()
                )
            )
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
        val templateClass = classService.createClass(
            id = "ContributionTemplate",
            label = "Contribution Template"
        )
        val throwAwayClass = classService.createClass(label = "Templated Class")
        val templateLabelPredicate = predicateService.createPredicate(
            id = "TemplateLabelFormat",
            label = "Template label format"
        )
        val templateClassPredicate = predicateService.createPredicate(
            id = "TemplateOfClass",
            label = "Template of class"
        )
        val templateComponentPredicate = predicateService.createPredicate(
            id = "TemplateComponent",
            label = "Template component"
        )
        val templateComponentClass = classService.createClass(
            id = "TemplateComponentClass",
            label = "Template component class"
        )
        val throwAwayProperty = predicateService.create("Temp property")
        val templateComponentPropertyPredicate = predicateService.createPredicate(
            id = "TemplateComponentProperty",
            label = "Template component property"
        )
        // create the template
        val template = service.createResource(
            classes = setOf(templateClass.value),
            label = "Throw-way template"
        )
        val labelFormat = literalService.create("xx{${throwAwayProperty.id}}xx")
        statementService.create(template.value, templateLabelPredicate, labelFormat.id.value)
        statementService.create(template.value, templateClassPredicate, throwAwayClass.value)
        val templateComponent = service.createResource(
            classes = setOf(templateComponentClass.value),
            label = "component 1"
        )
        statementService.create(template.value, templateComponentPredicate, templateComponent.value)
        statementService.create(templateComponent.value, templateComponentPropertyPredicate, throwAwayProperty.id.value)
        // Create resource and type it
        val templatedResource = service.createResource(
            classes = setOf(throwAwayClass.value),
            label = "Fancy resource"
        )
        val someValue = literalService.create(value)
        statementService.create(templatedResource.value, throwAwayProperty.id, someValue.id.value)
        return templatedResource
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
