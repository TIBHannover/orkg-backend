package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.createClasses
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Template Controller")
@Transactional
@Import(MockUserDetailsService::class)
class TemplateControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var userService: AuthUseCase

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var templateService: TemplateUseCases

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)

        listOf(
            Predicates.hasAuthor,
            Predicates.hasAuthors,
            Predicates.hasResearchField,
            Predicates.description,
            Predicates.shClass,
            Predicates.shClosed,
            Predicates.shDatatype,
            Predicates.shMaxCount,
            Predicates.shMinCount,
            Predicates.shOrder,
            Predicates.shPath,
            Predicates.shPattern,
            Predicates.shProperty,
            Predicates.shTargetClass,
            Predicates.templateLabelFormat,
            Predicates.templateOfPredicate,
            Predicates.templateOfResearchField,
            Predicates.templateOfResearchProblem,
            Predicates.hasResearchProblem,
            Predicates.field,
            Predicates.hasContribution
        ).forEach { predicateService.createPredicate(it) }

        classService.createClasses(Classes.nodeShape.value, Classes.propertyShape.value, "Problem", "ResearchField")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        classService.createClasses("C123", "C24", "C25", "C27", "C28", "C456")

        resourceService.createResource(id = "R15", classes = setOf(Classes.problem.value))
        resourceService.createResource(id = "R16", classes = setOf(Classes.problem.value))
        resourceService.createResource(
            id = "R13",
            label = "Engineering",
            classes = setOf("ResearchField")
        )

        val userId = userService.createUser()

        organizationService.createOrganization(
            createdBy = ContributorId(userId),
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
        )

        observatoryService.createObservatory(
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
            researchField = ThingId("R12"),
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
        )
    }

    @AfterEach
    fun cleanup() {
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        observatoryService.removeAll()
        organizationService.removeAll()
        userRepository.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndUpdate() {
        val id = createTemplate()

        get("/api/templates/{id}", id)
            .content(createTemplateJson)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplate()

        put("/api/templates/{id}", id)
            .content(updateTemplateJson)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateLiteralProperty() {
        val templateId = ThingId(createTemplate())

        post("/api/templates/$templateId/properties")
            .content(createLiteralTemplatePropertyJson)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(templateId)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }
        val propertyId = template.properties.first().id

        put("/api/templates/$templateId/properties/$propertyId")
            .content(updateLiteralTemplatePropertyJson)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateResourceProperty() {
        val templateId = ThingId(createTemplate())

        post("/api/templates/$templateId/properties")
            .content(createResourceTemplatePropertyJson)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(templateId)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }
        val propertyId = template.properties.first().id

        put("/api/templates/$templateId/properties/$propertyId")
            .content(updateResourceTemplatePropertyJson)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    private fun createTemplate(): String =
        post("/api/templates")
            .content(createTemplateJson)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createTemplateJson = """{
  "label": "example template",
  "description": "template description",
  "formatted_label": "{P32}",
  "target_class": "C123",
  "relations": {
    "research_fields": ["R12"],
    "research_problems": ["R15"],
    "predicate": "P32"
  },
  "properties": [
    {
      "label": "literal property label",
      "min_count": 1,
      "max_count": 2,
      "pattern": "\\d+",
      "path": "P24",
      "datatype": "C25"
    },
    {
      "label": "resource property label",
      "min_count": 3,
      "max_count": 4,
      "pattern": "\\w+",
      "path": "P27",
      "class": "C28"
    }
  ],
  "is_closed": false,
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ]
}"""

private const val updateTemplateJson = """{
  "label": "updated example template",
  "description": "updated template description",
  "formatted_label": "{P34}",
  "target_class": "C456",
  "relations": {
    "research_fields": ["R13"],
    "research_problems": ["R16"],
    "predicate": "P31"
  },
  "properties": [
    {
      "label": "updated resource property label",
      "min_count": 3,
      "max_count": 4,
      "pattern": "\\w+",
      "path": "P27",
      "class": "C28"
    },
    {
      "label": "updated literal property label",
      "min_count": 1,
      "max_count": 2,
      "pattern": "\\d+",
      "path": "P24",
      "datatype": "C25"
    }
  ],
  "is_closed": false,
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ]
}"""

private const val createLiteralTemplatePropertyJson = """{
  "label": "literal property label",
  "min_count": 1,
  "max_count": 2,
  "pattern": "\\d+",
  "path": "P24",
  "datatype": "C25"
}"""

private const val updateLiteralTemplatePropertyJson = """{
  "label": "updated literal property label",
  "min_count": 1,
  "max_count": 1,
  "pattern": "\\w+",
  "path": "P31",
  "datatype": "C28"
}"""

private const val createResourceTemplatePropertyJson = """{
  "label": "resource property label",
  "min_count": 3,
  "max_count": 4,
  "pattern": "\\w+",
  "path": "P27",
  "class": "C27"
}"""

private const val updateResourceTemplatePropertyJson = """{
  "label": "updated resource property label",
  "min_count": 4,
  "max_count": 5,
  "pattern": "\\d+",
  "path": "P27",
  "class": "C24"
}"""
