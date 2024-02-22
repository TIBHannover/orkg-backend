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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
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

        predicateService.createPredicate(Predicates.hasDOI)
        predicateService.createPredicate(Predicates.hasAuthors)
        predicateService.createPredicate(Predicates.monthPublished)
        predicateService.createPredicate(Predicates.yearPublished)
        predicateService.createPredicate(Predicates.hasResearchField)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.hasURL)
        predicateService.createPredicate(Predicates.hasResearchProblem)
        predicateService.createPredicate(Predicates.hasEvaluation)
        predicateService.createPredicate(Predicates.hasORCID)
        predicateService.createPredicate(Predicates.hasVenue)
        predicateService.createPredicate(Predicates.hasWebsite)
        predicateService.createPredicate(Predicates.description)
        predicateService.createPredicate(Predicates.hasListElement)

        classService.createClasses("Paper", "Contribution", "Problem", "ResearchField", "Author", "Venue", "Result")

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf(Classes.researchField.value)
        )
        resourceService.createResource(
            id = "R194",
            label = "Engineering",
            classes = setOf(Classes.researchField.value)
        )

        // Example specific entities

        classService.createClasses("C123")

        resourceService.createResource(id = "R3003", label = "Some resource")
        resourceService.createResource(id = "R3004", label = "Some other resource")

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
    @WithUserDetails(userDetailsServiceBeanName = "mockUserDetailsService")
    fun createAndUpdate() {
        val id = post("/api/papers")
            .content(createPaperJson)
            .accept("application/vnd.orkg.paper.v2+json")
            .contentType("application/vnd.orkg.paper.v2+json")
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")

        put("/api/papers/{id}", id)
            .content(updatePaperJson)
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
            .andExpect(status().isCreated)
    }

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createPaperJson = """{
  "title": "example paper",
  "research_fields": [
    "R12"
  ],
  "identifiers": {
    "doi": ["10.48550/arXiv.2304.05327"]
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

private const val updatePaperJson = """{
  "title": "updated paper title",
  "research_fields": [
    "R194"
  ],
  "identifiers": {
    "doi": ["10.48550/arXiv.2304.05328"]
  },
  "publication_info": {
    "published_month": 6,
    "published_year": 2016,
    "published_in": "other conference",
    "url": "https://www.conference.org"
  },
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
        "orcid": ["4444-3333-2222-1111"]
      }
    },
    {
      "name": "Author with homepage",
      "homepage": "http://example.org/author"
    },
    {
      "name": "Another author that just has a name"
    }
  ],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ]
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
