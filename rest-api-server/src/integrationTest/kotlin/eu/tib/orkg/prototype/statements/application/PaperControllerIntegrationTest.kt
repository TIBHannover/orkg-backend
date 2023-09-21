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

@DisplayName("Paper Controller")
@Transactional
@Import(MockUserDetailsService::class)
class PaperControllerIntegrationTest : RestDocumentationBaseTest() {

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

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)

        listOf(
            Predicates.hasDOI,
            Predicates.hasAuthors,
            Predicates.monthPublished,
            Predicates.yearPublished,
            Predicates.hasResearchField,
            Predicates.hasContribution,
            Predicates.hasURL,
            Predicates.hasResearchProblem,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.hasVenue,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement
        ).forEach { predicateService.createPredicate(label = it.value, id = it.value) }

        classService.createClasses("Paper", "Contribution", "Problem", "ResearchField", "Author", "Venue", "Result")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        classService.createClasses("C123")

        resourceService.createResource(id = "R3003", label = "Some resource")
        resourceService.createResource(id = "R3004", label = "Some other resource")

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
        post("/api/papers")
            .content(createPaperJson)
            .accept("application/vnd.orkg.paper.v2+json")
            .contentType("application/vnd.orkg.paper.v2+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "mockUserDetailsService")
    fun createContribution() {
        val paperId = resourceService.createResource(
            id = "R165487",
            label = "Some other resource",
            classes = setOf("Paper")
        )

        post("/api/papers/$paperId/contributions")
            .content(createContributionJson)
            .accept("application/vnd.orkg.contribution.v2+json")
            .contentType("application/vnd.orkg.contribution.v2+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isNoContent)
    }

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createPaperJson = """{
  "title": "example paper",
  "research_fields": [
    "R12"
  ],
  "identifiers": {
    "doi": "10.48550/arXiv.2304.05327"
  },
  "publication_info": {
    "published_month": 5,
    "published_year": 2015,
    "published_in": "conference",
    "url": "https://www.example.org"
  },
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
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "contents": {
    "resources": {
      "#temp1": {
        "label": "MOTO",
        "classes": ["Result"]
      }
    },
    "literals": {
      "#temp2": {
        "label": "0.1",
        "data_type": "xsd:decimal"
      }
    },
    "predicates": {
      "#temp3": {
        "label": "hasResult",
        "description": "has result"
      }
    },
    "lists": {
      "#temp4": {
        "label": "list",
        "elements": ["#temp1", "C123"]
      }
    },
    "contributions": [
      {
        "label": "Contribution 1",
        "classes": ["C123"],
        "statements": {
          "P32": [
            {
              "id": "R3003"
            }
          ],
          "HAS_EVALUATION": [
            {
              "id": "#temp1"
            },
            {
              "id": "R3004",
              "statements": {
                "#temp3": [
                  {
                    "id": "R3003"
                  },
                  {
                    "id": "#temp2"
                  },
                  {
                    "id": "#temp4"
                  }
                ],
                "P32": [
                  {
                    "id": "#temp2"
                  }
                ]
              }
            }
          ]
        }
      }
    ]
  },
  "extraction_method": "MANUAL"
}"""

private const val createContributionJson = """{
  "resources": {
    "#temp1": {
      "label": "MOTO",
      "classes": ["Result"]
    }
  },
  "literals": {
    "#temp2": {
      "label": "0.1",
      "data_type": "xsd:decimal"
    }
  },
  "predicates": {
    "#temp3": {
      "label": "hasResult",
      "description": "has result"
    }
  },
  "lists": {
    "#temp4": {
      "label": "list",
      "elements": ["#temp1", "C123"]
    }
  },
  "contribution": {
    "label": "Contribution 1",
    "statements": {
      "HAS_EVALUATION": [
        {
          "id": "#temp1"
        },
        {
          "id": "R3004",
          "statements": {
            "#temp3": [
              {
                "id": "R3003"
              },
              {
                "id": "#temp2"
              },
              {
                "id": "#temp4"
              }
            ],
            "P32": [
              {
                "id": "#temp2"
              }
            ]
          }
        }
      ]
    }
  }
}"""