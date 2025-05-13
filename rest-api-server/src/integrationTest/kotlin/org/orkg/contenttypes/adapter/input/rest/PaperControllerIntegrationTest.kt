package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
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
import org.orkg.common.testing.fixtures.Assets.requestJson
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
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
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
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class PaperControllerIntegrationTest : MockMvcBaseTest("papers") {
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
        assertThat(organizationService.findAll()).hasSize(0)
        assertThat(organizationService.findAllConferences()).hasSize(0)

        predicateService.createPredicates(
            Predicates.hasDOI,
            Predicates.hasISBN,
            Predicates.hasISSN,
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
            Predicates.hasListElement,
            Predicates.sustainableDevelopmentGoal,
            Predicates.mentions,
        )

        classService.createClasses(
            Classes.paper,
            Classes.contribution,
            Classes.problem,
            Classes.researchField,
            Classes.author,
            Classes.venue,
            Classes.sustainableDevelopmentGoal,
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField)
        )
        resourceService.createResource(
            id = ThingId("R194"),
            label = "Engineering",
            classes = setOf(Classes.researchField)
        )

        // Example specific entities

        classService.createClasses(
            ThingId("C123"),
            ThingId("Result")
        )

        resourceService.createResource(id = ThingId("R3003"), label = "Some resource")
        resourceService.createResource(id = ThingId("R3004"), label = "Some other resource")
        resourceService.createResource(id = ThingId("R3005"), label = "Some other more different resource", classes = setOf(Classes.problem))

        resourceService.createResource(id = ThingId("R123"), label = "Author with id", classes = setOf(Classes.author))
        resourceService.createResource(id = ThingId("SDG_1"), label = "No poverty", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_2"), label = "Zero hunger", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_3"), label = "Good health and well-being", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_4"), label = "Quality education", classes = setOf(Classes.sustainableDevelopmentGoal))

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R456"),
                label = "Author with id and orcid",
                classes = setOf(Classes.author)
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "1111-2222-3333-4444")
        )

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R4567"),
                label = "Author with orcid",
                classes = setOf(Classes.author)
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
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
        observatoryService.deleteAll()
        organizationService.deleteAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdate() {
        val id = post("/api/papers")
            .content(requestJson("orkg/createPaper"))
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
            .content(requestJson("orkg/updatePaper"))
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
            it.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.verified shouldBe false
            it.visibility shouldBe Visibility.DELETED
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }
    }
}
