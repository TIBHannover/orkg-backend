package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.net.URI
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
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("SmartReview Controller")
@Transactional
@Import(MockUserDetailsService::class)
class SmartReviewControllerIntegrationTest : RestDocumentationBaseTest() {

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
    private lateinit var smartReviewService: SmartReviewUseCases

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

        predicateService.createPredicate(Predicates.description)
        predicateService.createPredicate(Predicates.hasAuthors)
        predicateService.createPredicate(Predicates.hasContent)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.hasEntity)
        predicateService.createPredicate(Predicates.hasLink)
        predicateService.createPredicate(Predicates.hasListElement)
        predicateService.createPredicate(Predicates.hasORCID)
        predicateService.createPredicate(Predicates.hasReference)
        predicateService.createPredicate(Predicates.hasResearchField)
        predicateService.createPredicate(Predicates.hasSection)
        predicateService.createPredicate(Predicates.hasURL)
        predicateService.createPredicate(Predicates.hasWebsite)
        predicateService.createPredicate(Predicates.showProperty)
        predicateService.createPredicate(Predicates.sustainableDevelopmentGoal)

        classService.createClasses(
            "SmartReview",
            "Paper",
            "Comparison",
            "Visualization",
            "Dataset",
            "ResearchField",
            "Author",
            Classes.sustainableDevelopmentGoal.value,
            *SmartReviewTextSection.types.map { it.value }.toTypedArray()
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

        resourceService.createResource(id = "R6416", label = "Some comparison", classes = setOf("Comparison"))
        resourceService.createResource(id = "R215648", label = "Some visualization", classes = setOf("Visualization"))
        resourceService.createResource(id = "R14565", label = "Some dataset resource", classes = setOf("Dataset"))
        resourceService.createResource(id = "R1", label = "Some ontology resource")

        predicateService.createPredicate(id = ThingId("R15696541"), label = "Some predicate")
        predicateService.createPredicate(id = ThingId("P1"), label = "Some ontology predicate")

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
    fun createAndFetch() {
        val id = createSmartReview()

        val smartReview = get("/api/smart-reviews/{id}", id)
            .accept(SMART_REVIEW_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, SmartReviewRepresentation::class.java) }

        smartReview.asClue {
            it.id shouldBe id
            it.title shouldBe "example smart review"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R12"), "Computer Science")
            )
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
                author.homepage shouldBe URI("http://example.org/author")
            }
            it.authors[4] shouldBe AuthorRepresentation(
                name = "Author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                LabeledObjectRepresentation(ThingId("SDG_1"), "No poverty"),
                LabeledObjectRepresentation(ThingId("SDG_2"), "Zero hunger")
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
            it.published shouldBe false
            it.sections.size shouldBe 6
            it.sections[0].shouldBeInstanceOf<SmartReviewComparisonSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "comparison section heading"
                section.comparison shouldBe ResourceReferenceRepresentation(ThingId("R6416"), "Some comparison", setOf(Classes.comparison))
            }
            it.sections[1].shouldBeInstanceOf<SmartReviewVisualizationSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "visualization section heading"
                section.visualization shouldBe ResourceReferenceRepresentation(ThingId("R215648"), "Some visualization", setOf(Classes.visualization))
            }
            it.sections[2].shouldBeInstanceOf<SmartReviewResourceSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "resource section heading"
                section.resource shouldBe ResourceReferenceRepresentation(ThingId("R14565"), "Some dataset resource", setOf(Classes.dataset))
            }
            it.sections[3].shouldBeInstanceOf<SmartReviewPredicateSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "predicate section heading"
                section.predicate shouldBe PredicateReferenceRepresentation(ThingId("R15696541"), "Some predicate")
            }
            it.sections[4].shouldBeInstanceOf<SmartReviewOntologySectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "ontology section heading"
                section.entities shouldBe listOf(
                    ResourceReferenceRepresentation(ThingId("R1"), "Some ontology resource", emptySet()),
                    PredicateReferenceRepresentation(ThingId("P1"), "Some ontology predicate")
                )
                section.predicates shouldBe listOf(PredicateReferenceRepresentation(ThingId("P1"), "Some ontology predicate"))
            }
            it.sections[5].shouldBeInstanceOf<SmartReviewTextSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "Heading"
                section.classes shouldBe setOf(Classes.introduction)
                section.text shouldBe "text section contents"
            }
            it.references shouldBe listOf(
                "@misc{R615465, title = {reference 1}}",
                "@misc{R154146, title = {reference 2}}"
            )
        }
    }

    private fun createSmartReview() = post("/api/smart-reviews")
        .content(createSmartReviewJson)
        .accept(SMART_REVIEW_JSON_V1)
        .contentType(SMART_REVIEW_JSON_V1)
        .characterEncoding("utf-8")
        .perform()
        .andExpect(status().isCreated)
        .andReturn()
        .response
        .getHeaderValue("Location")!!
        .toString()
        .substringAfterLast("/")
        .let(::ThingId)

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createSmartReviewJson = """{
  "title": "example smart review",
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
  "sdgs": ["SDG_1", "SDG_2"],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "extraction_method": "MANUAL",
  "sections": [
    {
      "heading": "comparison section heading",
      "comparison": "R6416"
    },
    {
      "heading": "visualization section heading",
      "visualization": "R215648"
    },
    {
      "heading": "resource section heading",
      "resource": "R14565"
    },
    {
      "heading": "predicate section heading",
      "predicate": "R15696541"
    },
    {
      "heading": "ontology section heading",
      "entities": ["R1", "P1"],
      "predicates": ["P1"]
    },
    {
      "heading": "Heading",
      "class": "Introduction",
      "text": "text section contents"
    }
  ],
  "references": [
    "@misc{R615465, title = {reference 1}}",
    "@misc{R154146, title = {reference 2}}"
  ]
}"""
