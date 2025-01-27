package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.createClass
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
@TestPropertySource(properties = ["orkg.features.formatted_labels=false"])
internal class ResourceControllerIntegrationTest : RestDocsTest("resources") {

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

    @Autowired
    @Suppress("unused")
    private lateinit var unsafeResourceUseCases: UnsafeResourceUseCases

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

        get("/api/resources/{id}", id)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    @TestWithMockUser
    fun add() {
        val resource = mapOf("label" to "foo")

        documentedPostRequestTo("/api/resources")
            .content(resource)
            .perform()
            .andExpect(status().isCreated)
//            TODO: Needs to be converted into a unit test in order to test location header path
//            .andExpect(header().string("Location", endsWith("api/resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the created resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The resource label."),
                        fieldWithPath("classes").type("Array").description("The classes of the resource. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional, default: "UNKNOWN")""").optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @TestWithMockUser
    fun addButClassDoesNotExist() {
        val resource = mapOf(
            "label" to "foo",
            "classes" to setOf(ThingId("doesNotExist"))
        )

        post("/api/resources")
            .content(resource)
            .perform()
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockUser
    fun addWithExistingIds() {
        val resource = mapOf("label" to "bar", "id" to "Test")

        service.createResource(id = "Test", label = "foo")

        post("/api/resources")
            .content(resource)
            .perform()
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockUser
    fun edit() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "foo")
        val newLabel = "bar"
        val update = mapOf(
            "label" to newLabel,
            "classes" to setOf(oldClass),
            "extraction_method" to ExtractionMethod.UNKNOWN,
            "visibility" to Visibility.DEFAULT
        )

        put("/api/resources/{id}", resource)
            .content(update)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(newLabel))
    }

    @Test
    @TestWithMockUser
    fun editResourceClass() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val newClass = classService.createClass("clazz")
        val update = mapOf("classes" to listOf(newClass))

        put("/api/resources/{id}", resource)
            .content(update)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("test"))
            .andExpect(jsonPath("$.classes[0]").value(newClass.value))
    }

    @Test
    @TestWithMockUser
    fun editResourceClassesIsEmpty() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to emptyList<ThingId>())

        put("/api/resources/{id}", resource)
            .content(update)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.classes", hasSize<Int>(0)))
    }

    @Test
    @TestWithMockUser
    fun editResourceClassesAreInvalid() {
        val oldClass = classService.createClass(label = "class")
        val resource = service.createResource(classes = setOf(oldClass.value), label = "test")

        val update = mapOf("classes" to setOf(ThingId("DoesNotExist")))

        put("/api/resources/{id}", resource)
            .content(update)
            .perform()
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockAdmin
    fun deleteResourceNotFound() {
        delete("/api/resources/{id}", "NONEXISTENT")
            .perform()
            .andExpect(status().isNotFound)
    }

    @Test
    @TestWithMockAdmin
    fun deleteResourceSuccess() {
        val id = service.createResource(label = "bye bye", userId = ContributorId(MockUserId.ADMIN))

        documentedDeleteRequestTo("/api/resources/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @TestWithMockAdmin
    fun deleteResourceForbidden() {
        val subject = service.createResource(label = "parent")
        val `object` = service.createResource(label = "son")
        val predicate = predicateService.createPredicate(label = "related")
        statementService.create(subject, predicate, `object`)

        delete("/api/resources/{id}", `object`)
            .perform()
            .andExpect(status().isForbidden)
    }

    @Test
    fun deleteResourceWithoutLogin() {
        val id = service.createResource(label = "To Delete")

        delete("/api/resources/{id}", id)
            .perform()
            .andExpect(status().isForbidden)
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

        get("/api/resources")
            .param("q", "Resource")
            .param("exclude", "$id")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[?(@.label == 'Resource 3')].shared").value(2))
            .andExpect(jsonPath("$.content[?(@.label == 'Another Resource')].shared").value(0))
    }

    @Test
    fun `fetch resource with the correct formatted label`() {
        val value = "Wow!"
        val id = createTemplateAndTypedResource(value)

        get("/api/resources/{id}", id)
            .accept("application/json;formatted-labels=V1")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.formatted_label").value("xx${value}xx"))
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
        fun pageOfDetailedResourcesResponseFields(): ResponseFieldsSnippet {
            return responseFields(pageableDetailedFieldParameters())
                .andWithPrefix(
                    "content[].", resourceResponseFields()
                ).andWithPrefix("")
        }
    }
}
