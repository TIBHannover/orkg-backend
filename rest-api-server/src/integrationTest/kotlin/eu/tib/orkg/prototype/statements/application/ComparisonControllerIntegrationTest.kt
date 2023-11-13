package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createObservatory
import eu.tib.orkg.prototype.createOrganization
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createUser
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Comparison Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ComparisonControllerIntegrationTest : RestDocumentationBaseTest() {

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
            Predicates.hasResearchField,
            Predicates.comparesContribution,
            Predicates.hasResearchProblem,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.reference,
            Predicates.isAnonymized,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement
        ).forEach { predicateService.createPredicate(label = it.value, id = it.value) }

        classService.createClasses("Comparison", "Contribution", "Problem", "ResearchField", "Author", "Venue", "Result")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        resourceService.createResource(id = "R6541", label = "Contribution 1", classes = setOf(Classes.contribution.value))
        resourceService.createResource(id = "R5364", label = "Contribution 2", classes = setOf(Classes.contribution.value))
        resourceService.createResource(id = "R9786", label = "Contribution 3", classes = setOf(Classes.contribution.value))
        resourceService.createResource(id = "R3120", label = "Contribution 4", classes = setOf(Classes.contribution.value))

        resourceService.createResource(id = "R123", label = "Author with id", classes = setOf("Author"))

        statementService.create(
            subject = resourceService.createResource(id = "R456", label = "Author with id and orcid", classes = setOf("Author")),
            predicate = Predicates.hasORCID,
            `object` = literalService.create("1111-2222-3333-4444").id
        )

        statementService.create(
            subject = resourceService.createResource(id = "R456", label = "Author with orcid", classes = setOf("Author")),
            predicate = Predicates.hasORCID,
            `object` = literalService.create("0000-1111-2222-3333").id
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
        post("/api/comparisons")
            .content(createComparisonJson)
            .accept("application/vnd.orkg.comparison.v2+json")
            .contentType("application/vnd.orkg.comparison.v2+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createComparisonJson = """{
  "title": "example comparison",
  "description": "comparison description",
  "research_fields": [
    "R12"
  ],
  "authors": [
    {
      "name": "Author with id",
      "id": "R123"
    },
    {
      "name": "Author with orcid",
      "identifiers": {
        "orcid": "0000-1111-2222-3333"
      }
    },
    {
      "name": "Author with id and orcid",
      "id": "R456",
      "identifiers": {
        "orcid": "1111-2222-3333-4444"
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
  "contributions": [
    "R6541", "R5364", "R9786", "R3120"
  ],
  "references": [
    "https://orkg.org/resources/R1000",
    "paper citation"
  ],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "is_anonymized": false,
  "extraction_method": "MANUAL"
}"""
