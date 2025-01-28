package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
internal class PaperControllerIntegrationTest : RestDocsTest("papers") {

    @Autowired
    private lateinit var contributorService: ContributorUseCases

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
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var paperService: PaperUseCases

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
        predicateService.createPredicate(Predicates.hasISBN)
        predicateService.createPredicate(Predicates.hasISSN)
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
        predicateService.createPredicate(Predicates.sustainableDevelopmentGoal)
        predicateService.createPredicate(Predicates.mentions)

        classService.createClasses(
            "Paper",
            "Contribution",
            "Problem",
            "ResearchField",
            "Author",
            "Venue",
            "Result",
            Classes.sustainableDevelopmentGoal.value
        )

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
        resourceService.createResource(id = "R3005", label = "Some other more different resource", classes = setOf(Classes.problem.value))

        resourceService.createResource(id = "R123", label = "Author with id", classes = setOf("Author"))
        resourceService.createResource(id = "SDG_1", label = "No poverty", classes = setOf(Classes.sustainableDevelopmentGoal.value))
        resourceService.createResource(id = "SDG_2", label = "Zero hunger", classes = setOf(Classes.sustainableDevelopmentGoal.value))
        resourceService.createResource(id = "SDG_3", label = "Good health and well-being", classes = setOf(Classes.sustainableDevelopmentGoal.value))
        resourceService.createResource(id = "SDG_4", label = "Quality education", classes = setOf(Classes.sustainableDevelopmentGoal.value))

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

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            createdBy = contributorId,
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
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdate() {
        val id = post("/api/papers")
            .content(createPaperJson)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val paper = get("/api/papers/{id}", id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, PaperRepresentation::class.java) }

        paper.asClue {
            it.id shouldBe id
            it.title shouldBe "example paper"
            it.researchFields shouldBe listOf(
                LabeledObjectRepresentation(ThingId("R12"), "Computer Science")
            )
            it.identifiers shouldBe mapOf(
                "doi" to listOf("10.48550/arXiv.2304.05327")
            )
            it.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe 5
                publicationInfo.publishedYear shouldBe 2015
                publicationInfo.publishedIn shouldNotBe null
                publicationInfo.publishedIn!!.asClue { publishedIn ->
                    publishedIn.id shouldNotBe null
                    publishedIn.label shouldBe "conference"
                }
                publicationInfo.url shouldBe ParsedIRI("https://www.example.org")
            }
            it.authors.size shouldBe 5
            it.authors[0] shouldBe AuthorRepresentation(
                name = "Author with id",
                id = ThingId("R123"),
                identifiers = emptyMap(),
                homepage = null
            )
            it.authors[1] shouldBe AuthorRepresentation(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null
            )
            it.authors[2] shouldBe AuthorRepresentation(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                homepage = null
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe ParsedIRI("https://example.org/author")
            }
            it.authors[4] shouldBe AuthorRepresentation(
                name = "Author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null
            )
            it.contributions.asClue { contributions ->
                contributions.size shouldBe 1
                contributions[0].asClue { contribution ->
                    contribution.id shouldNotBe null
                    contribution.label shouldBe "Contribution 1"
                }
            }
            it.sustainableDevelopmentGoals shouldBe setOf(
                LabeledObjectRepresentation(ThingId("SDG_1"), "No poverty"),
                LabeledObjectRepresentation(ThingId("SDG_2"), "Zero hunger")
            )
            it.mentionings shouldBe setOf(
                ResourceReferenceRepresentation(ThingId("R3003"), "Some resource", emptySet()),
                ResourceReferenceRepresentation(ThingId("R3004"), "Some other resource", emptySet())
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.verified shouldBe false
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        put("/api/papers/{id}", id)
            .content(updatePaperJson)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)

        val updatedPaper = paperService.findById(id).orElseThrow { PaperNotFound(id) }

        updatedPaper.asClue {
            it.id shouldBe id
            it.title shouldBe "updated paper title"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R194"), "Engineering")
            )
            it.identifiers shouldBe mapOf(
                "doi" to listOf("10.48550/arXiv.2304.05328"),
                "isbn" to listOf("978-123456789-0"),
                "issn" to listOf("1234-5678")
            )
            it.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe 6
                publicationInfo.publishedYear shouldBe 2016
                publicationInfo.publishedIn shouldNotBe null
                publicationInfo.publishedIn!!.asClue { publishedIn ->
                    publishedIn.id shouldNotBe null
                    publishedIn.label shouldBe "other conference"
                }
                publicationInfo.url shouldBe ParsedIRI("https://www.conference.org")
            }
            it.authors.size shouldBe 5
            it.authors[0] shouldBe Author(
                name = "Author with id",
                id = ThingId("R123"),
                identifiers = emptyMap(),
                homepage = null
            )
            it.authors[1] shouldBe Author(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null
            )
            it.authors[2] shouldBe Author(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444", "4444-3333-2222-1111")),
                homepage = null
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe ParsedIRI("https://example.org/author")
            }
            it.authors[4] shouldBe Author(
                name = "Another author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null
            )
            it.contributions.asClue { contributions ->
                contributions.size shouldBe 1
                contributions[0].asClue { contribution ->
                    contribution.id shouldNotBe null
                    contribution.label shouldBe "Contribution 1"
                }
            }
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"),
                ObjectIdAndLabel(ThingId("SDG_4"), "Quality education")
            )
            it.mentionings shouldBe setOf(
                ResourceReference(ThingId("R3004"), "Some other resource", emptySet()),
                ResourceReference(ThingId("R3005"), "Some other more different resource", setOf(Classes.problem))
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.verified shouldBe false
            it.visibility shouldBe Visibility.DELETED
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchContribution() {
        val paperId = resourceService.createResource(
            id = "R165487",
            label = "Some other resource",
            classes = setOf("Paper")
        )

        val id = post("/api/papers/$paperId/contributions")
            .content(createContributionJson)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val contribution = get("/api/contributions/{id}", id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, ContributionRepresentation::class.java) }

        contribution.asClue {
            it.id shouldBe id
            it.label shouldBe "Contribution 1"
            it.classes shouldBe setOf(Classes.contribution)
            it.properties shouldNotBe null
            it.properties[Predicates.hasEvaluation].asClue { property ->
                property shouldNotBe null
                property!!.size shouldBe 2
                property shouldContain ThingId("R3004")
            }
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
        }
    }
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
      "homepage": "https://example.org/author"
    },
    {
      "name": "Author that just has a name"
    }
  ],
  "sdgs": ["SDG_1", "SDG_2"],
  "mentionings": ["R3003", "R3004"],
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
    "doi": ["10.48550/arXiv.2304.05328"],
    "isbn": ["978-123456789-0"],
    "issn": ["1234-5678"]
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
      "homepage": "https://example.org/author"
    },
    {
      "name": "Another author that just has a name"
    }
  ],
  "sdgs": ["SDG_3", "SDG_4"],
  "mentionings": ["R3004", "R3005"],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "visibility": "DELETED"
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
