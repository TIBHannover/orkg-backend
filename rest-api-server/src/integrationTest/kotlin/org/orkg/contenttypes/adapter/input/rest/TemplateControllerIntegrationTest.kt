package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions
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
import org.orkg.createClasses
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var userService: AuthUseCase

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        Assertions.assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(classService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(organizationService.listOrganizations()).hasSize(0)
        Assertions.assertThat(organizationService.listConferences()).hasSize(0)

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
            Predicates.templateOfResearchProblem
        ).forEach { predicateService.createPredicate(label = it.value, id = it.value) }

        classService.createClasses(Classes.nodeShape.value, Classes.propertyShape.value, "Problem", "ResearchField")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        classService.createClasses("C123", "C25", "C28")

        predicateService.createPredicates("P24", "P32")

        resourceService.createResource(id = "R15", classes = setOf(Classes.problem.value))

        val userId = userService.createUser()

        organizationService.createOrganization(
            createdBy = ContributorId(userId),
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
        )

        observatoryService.createObservatory(
            organizationId = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
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
    @WithUserDetails(userDetailsServiceBeanName = "mockUserDetailsService")
    fun create() {
        MockMvcRequestBuilders.post("/api/templates")
            .content(createTemplateJson)
            .accept("application/vnd.orkg.template.v1+json")
            .contentType("application/vnd.orkg.template.v1+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "mockUserDetailsService")
    fun createLiteralProperty() {
        val templateId = resourceService.createResource(
            id = "R165487",
            classes = setOf(Classes.nodeShape.value)
        )

        MockMvcRequestBuilders.post("/api/templates/$templateId/properties")
            .content(createLiteralTemplatePropertyJson)
            .accept("application/vnd.orkg.template.property.v1+json")
            .contentType("application/vnd.orkg.template.property.v1+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "mockUserDetailsService")
    fun createResourceProperty() {
        val templateId = resourceService.createResource(
            id = "R165487",
            classes = setOf(Classes.nodeShape.value)
        )

        MockMvcRequestBuilders.post("/api/templates/$templateId/properties")
            .content(createResourceTemplatePropertyJson)
            .accept("application/vnd.orkg.template.property.v1+json")
            .contentType("application/vnd.orkg.template.property.v1+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

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
      "datatype": "C28"
    }
  ],
  "is_closed": true,
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

private const val createResourceTemplatePropertyJson = """{
  "label": "resource property label",
  "min_count": 3,
  "max_count": 4,
  "pattern": "\\w+",
  "path": "P27",
  "datatype": "C28"
}"""
