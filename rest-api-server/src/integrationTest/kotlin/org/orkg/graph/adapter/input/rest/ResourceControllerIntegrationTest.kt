package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.createClass
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Resource Controller")
@Transactional
@Import(MockUserDetailsService::class)
@TestPropertySource(properties = ["orkg.features.formatted_labels=false"])
class ResourceControllerIntegrationTest : RestDocumentationBaseTest() {

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
    @TestWithMockUser
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The resource label."),
                        fieldWithPath("classes").type("Array").description("The classes of the resource. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional, default: "UNKNOWN")""").optional()
                    ),
                    createdResponseHeaders(),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    @TestWithMockUser
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
    @TestWithMockUser
    fun addWithExistingIds() {
        val resource = mapOf("label" to "bar", "id" to "Test")

        service.createResource(id = "Test", label = "foo")

        mockMvc
            .perform(postRequestWithBody("/api/resources/", resource))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
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
                        fieldWithPath("label").description("The updated resource label. (optional)").optional(),
                        fieldWithPath("classes").description("The classes to which the resource belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional)""").optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    @WithMockUser
    fun editResourceClass() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val newClass = classService.createClass("clazz")
        val update = mapOf("classes" to listOf(newClass))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("test"))
            .andExpect(jsonPath("$.classes[0]").value(newClass.value))
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").type("String").description("The updated resource label. (optional)").optional(),
                        fieldWithPath("classes").description("The classes to which the resource belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional)""").optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
    }

    @Test
    @WithMockUser
    fun editResourceClassesIsEmpty() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to emptyList<ThingId>())

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.classes", hasSize<Int>(0)))
    }

    @Test
    @WithMockUser
    fun editResourceClassesAreInvalid() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to setOf(ThingId("DoesNotExist")))

        mockMvc
            .perform(putRequestWithBody("/api/resources/$resource", update))
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
        val id = service.createResource(label = "bye bye", userId = ContributorId(MockUserId.ADMIN))

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
        statementService.create(subject, predicate, `object`)

        mockMvc
            .perform(deleteRequest("/api/resources/$`object`"))
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
        statementService.create(con1, predicate, resId)
        statementService.create(con2, predicate, resId)
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
                    pageOfDetailedResourcesResponseFields()
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

    fun createTemplateAndTypedResource(value: String): ThingId {
        // create required classes and predicates
        val nodeShapeClass = classService.createClass(
            id = "NodeShape",
            label = "Node shape"
        )
        val throwAwayClass = classService.createClass(label = "Templated Class")
        val templateLabelPredicate = predicateService.createPredicate(
            id = Predicates.templateLabelFormat,
            label = "Template label format"
        )
        val targetClassPredicate = predicateService.createPredicate(
            id = Predicates.shTargetClass,
            label = "target class"
        )
        val propertyPredicate = predicateService.createPredicate(
            id = Predicates.shProperty,
            label = "property"
        )
        val propertyShapeClass = classService.createClass(
            id = "PropertyShape",
            label = "Property shape"
        )
        val throwAwayProperty = predicateService.createPredicate(label = "Temp property")
        val pathPredicate = predicateService.createPredicate(
            id = Predicates.shPath,
            label = "path"
        )
        // create the template
        val template = service.createResource(
            classes = setOf(nodeShapeClass.value),
            label = "Throw-way template"
        )
        val labelFormat = literalService.createLiteral(label = "xx{$throwAwayProperty}xx")
        statementService.create(template, templateLabelPredicate, labelFormat)
        statementService.create(template, targetClassPredicate, throwAwayClass)
        val templateComponent = service.createResource(
            classes = setOf(propertyShapeClass.value),
            label = "component 1"
        )
        statementService.create(template, propertyPredicate, templateComponent)
        statementService.create(templateComponent, pathPredicate, throwAwayProperty)
        // Create resource and type it
        val templatedResource = service.createResource(
            classes = setOf(throwAwayClass.value),
            label = "Fancy resource"
        )
        val someValue = literalService.createLiteral(label = value)
        statementService.create(templatedResource, throwAwayProperty, someValue)
        return templatedResource
    }

    companion object RestDoc {
        fun resourceResponseFields() = listOf(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label"),
            fieldWithPath("created_at").description("The resource creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the resource. All zeros if unknown."),
            fieldWithPath("classes").description("The list of classes the resource belongs to"),
            fieldWithPath("observatory_id").description("The ID of the observatory that maintains this resource."),
            fieldWithPath("extraction_method").description("""Method to extract this resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
            fieldWithPath("organization_id").description("The ID of the organization that maintains this resource."),
            fieldWithPath("shared").description("The number of times this resource is shared").optional(),
            fieldWithPath("_class").description("Class").optional(),
            fieldWithPath("verified").description("Determines if the resource was verified by a curator.").optional()
                .ignored(),
            fieldWithPath("visibility").description("""Visibility of this resource. Either of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED".""")
                .optional().ignored(),
            fieldWithPath("featured").description("Featured Value").optional().ignored(),
            fieldWithPath("unlisted").description("Unlisted Value").optional().ignored(),
            fieldWithPath("modifiable").description("Whether this resource can be modified.").optional().ignored(),
            fieldWithPath("formatted_label").type("String").description("The formatted label of the resource. See <<content-negotiation,Content Negotiation>> for information on how to obtain this value.").optional()
        )

        fun pageOfDetailedResourcesResponseFields(): ResponseFieldsSnippet {
            return responseFields(pageableDetailedFieldParameters())
                .andWithPrefix(
                    "content[].", resourceResponseFields()
                ).andWithPrefix("")
        }
    }
}
