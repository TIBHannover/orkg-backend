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
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createUser
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Visualization Controller")
@Transactional
@Import(MockUserDetailsService::class)
class VisualizationControllerIntegrationTest : RestDocumentationBaseTest() {

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

        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)

        listOf(
            Predicates.hasAuthors,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.reference,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement
        ).forEach { predicateService.createPredicate(it) }

        classService.createClasses("Visualization", "Contribution", "Problem", "ResearchField", "Author", "Venue", "Result")

        // Example specific entities

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        resourceService.createResource(id = "R123", label = "Author with id", classes = setOf("Author"))

        statementService.create(
            subject = resourceService.createResource(
                id = "R456",
                label = "Author with id and orcid",
                classes = setOf("Author")
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "1111-2222-3333-4444")
        )

        statementService.create(
            subject = resourceService.createResource(
                id = "R4567",
                label = "Author with orcid",
                classes = setOf("Author")
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "0000-1111-2222-3333")
        )

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
        statementService.removeAll()
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
        post("/api/visualizations")
            .content(createVisualizationJson)
            .accept("application/vnd.orkg.visualization.v2+json")
            .contentType("application/vnd.orkg.visualization.v2+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
    }

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createVisualizationJson = """{
  "title": "example visualization",
  "description": "visualization description",
  "authors": [
    {
      "name": "Author with id",
      "id": "R123"
    },
    {
      "name": "Author with orcid",
      "identifiers": {
        "orcid": ["0000-1111-2222-3333"]
      }
    },
    {
      "name": "Author with id and orcid",
      "id": "R456",
      "identifiers": {
        "orcid": ["1111-2222-3333-4444"]
      }
    },
    {
      "name": "Author with homepage",
      "homepage": "http://example.org/author"
    },
    {
      "name": "Author that just has a name"
    }
  ],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "extraction_method": "MANUAL"
}"""
