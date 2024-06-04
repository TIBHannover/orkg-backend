package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.auth.testing.fixtures.createUser
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.TemplateRelationsDefinition
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClasses
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Template Instance Controller")
@Transactional
@Import(MockUserDetailsService::class)
class TemplateInstanceControllerIntegrationTest : RestDocumentationBaseTest() {

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
    private lateinit var templateService: TemplateUseCases

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
            Predicates.hasURL,
            Predicates.placeholder
        ).forEach { predicateService.createPredicate(it) }

        classService.createClasses(Classes.nodeShape.value, Classes.propertyShape.value, "Problem", "ResearchField")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        classService.createClasses("String", "Test")

        val userId = userService.createUser()

        organizationService.createOrganization(
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
            createdBy = ContributorId(userId)
        )

        observatoryService.createObservatory(
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"),
            researchField = ThingId("R12"),
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
        )

        val targetClass = ThingId("Test")

        templateService.create(
            CreateTemplateUseCase.CreateCommand(
                contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                label = "Dummy Template Label",
                description = "Some description about the template",
                formattedLabel = FormattedLabel.of("{P32}"),
                targetClass = targetClass,
                relations = TemplateRelationsDefinition(
                    researchFields = listOf(ThingId("R12")),
                    researchProblems = emptyList(),
                    predicate = null
                ),
                properties = listOf(
                    StringLiteralPropertyDefinition(
                        label = "literal property label",
                        description = "literal property description",
                        placeholder = "literal property placeholder",
                        minCount = 1,
                        maxCount = 2,
                        pattern = """https?\:\/\/.*""",
                        path = Predicates.hasURL,
                        datatype = ThingId("String"),
                    )
                ),
                isClosed = true,
                observatories = listOf(
                    ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
                ),
                organizations = listOf(
                    OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
                )
            )
        )
        statementService.create(
            subject = resourceService.createResource(
                id = "R6458",
                classes = setOf(targetClass.value)
            ),
            predicate = Predicates.hasURL,
            `object` = literalService.createLiteral()
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
    fun update() {
        val templateId = resourceService.findAll(
            includeClasses = setOf(ThingId("NodeShape")),
            pageable = PageRequests.SINGLE
        ).single().id

        put("/api/templates/{templateId}/instances/{id}", templateId, "R6458")
            .content(updateTemplateInstanceJson)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val updateTemplateInstanceJson = """{
  "resources": null,
  "literals": {
    "#temp1": "https://orkg.org/"
  },
  "predicates": null,
  "lists": null,
  "classes": null,
  "statements": {
    "url": ["#temp1"]
  }
}"""
