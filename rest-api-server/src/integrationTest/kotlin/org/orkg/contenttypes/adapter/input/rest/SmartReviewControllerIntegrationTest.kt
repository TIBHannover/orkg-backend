package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.input.SmartReviewUseCases
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
internal class SmartReviewControllerIntegrationTest : RestDocsTest("smart-reviews") {

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
    private lateinit var smartReviewService: SmartReviewUseCases

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
            Classes.section.value,
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

        resourceService.createResource(id = "R26416", label = "Some other comparison", classes = setOf("Comparison"))
        resourceService.createResource(id = "R2215648", label = "Some other visualization", classes = setOf("Visualization"))
        resourceService.createResource(id = "R214565", label = "Some other dataset resource", classes = setOf("Dataset"))
        resourceService.createResource(id = "R21", label = "Some other ontology resource")

        predicateService.createPredicate(id = ThingId("R15696541"), label = "Some predicate")
        predicateService.createPredicate(id = ThingId("P1"), label = "Some ontology predicate")

        predicateService.createPredicate(id = ThingId("R215696541"), label = "Some other predicate")
        predicateService.createPredicate(id = ThingId("P21"), label = "Some other ontology predicate")

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
        val id = createSmartReview()

        val smartReview = get("/api/smart-reviews/{id}", id)
            .accept(SMART_REVIEW_JSON_V1)
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
            it.identifiers shouldBe emptyMap()
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
            it.versions shouldBe VersionInfoRepresentation(
                head = HeadVersionRepresentation(id, it.title, it.createdAt, it.createdBy),
                published = emptyList()
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
            it.acknowledgements shouldBe mapOf(
                ContributorId(MockUserId.USER) to (7.0 / 13.0),
                ContributorId.UNKNOWN to (6.0 / 13.0)
            )
        }

        put("/api/smart-reviews/{id}", id)
            .content(updateSmartReviewJson)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id).orElseThrow { SmartReviewNotFound(id) }

        updatedSmartReview.asClue {
            it.id shouldBe id
            it.title shouldBe "updated smart review title"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R194"), "Engineering")
            )
            it.identifiers shouldBe emptyMap()
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
            it.versions shouldBe VersionInfo(
                head = HeadVersion(id, it.title, it.createdAt, it.createdBy),
                published = emptyList()
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"),
                ObjectIdAndLabel(ThingId("SDG_4"), "Quality education")
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
            it.published shouldBe false
            it.sections[0].shouldBeInstanceOf<SmartReviewComparisonSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated comparison section heading"
                section.comparison shouldBe ResourceReference(ThingId("R26416"), "Some other comparison", setOf(Classes.comparison))
            }
            it.sections[1].shouldBeInstanceOf<SmartReviewVisualizationSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated visualization section heading"
                section.visualization shouldBe ResourceReference(ThingId("R2215648"), "Some other visualization", setOf(Classes.visualization))
            }
            it.sections[2].shouldBeInstanceOf<SmartReviewResourceSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated resource section heading"
                section.resource shouldBe ResourceReference(ThingId("R214565"), "Some other dataset resource", setOf(Classes.dataset))
            }
            it.sections[3].shouldBeInstanceOf<SmartReviewPredicateSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated predicate section heading"
                section.predicate shouldBe PredicateReference(ThingId("R215696541"), "Some other predicate")
            }
            it.sections[4].shouldBeInstanceOf<SmartReviewOntologySection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated ontology section heading"
                section.entities shouldBe listOf(
                    ResourceReference(ThingId("R21"), "Some other ontology resource", emptySet()),
                    PredicateReference(ThingId("P1"), "Some ontology predicate")
                )
                section.predicates shouldBe listOf(PredicateReference(ThingId("P21"), "Some other ontology predicate"))
            }
            it.sections[5].shouldBeInstanceOf<SmartReviewTextSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated text section heading"
                section.classes shouldBe setOf(Classes.introduction)
                section.text shouldBe "Introduction text section contents"
            }
            it.references shouldBe listOf(
                "@misc{R615465, title = {updated reference 1}}",
                "@misc{R154146, title = {updated reference 2}}"
            )
            it.acknowledgements shouldBe mapOf(
                ContributorId(MockUserId.USER) to (7.0 / 13.0),
                ContributorId.UNKNOWN to (6.0 / 13.0)
            )
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateComparisonSection() {
        val id = createSmartReview()

        post("/api/smart-reviews/$id/sections")
            .content(createComparisonSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewComparisonSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new comparison section heading"
            it.comparison shouldBe ResourceReference(ThingId("R6416"), "Some comparison", setOf(Classes.comparison))
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(updateComparisonSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewComparisonSection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated comparison section heading"
            it.comparison shouldBe ResourceReference(ThingId("R26416"), "Some other comparison", setOf(Classes.comparison))
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateVisualizationSection() {
        val smartReviewId = createSmartReview()

        post("/api/smart-reviews/$smartReviewId/sections")
            .content(createVisualizationSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(smartReviewId)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewVisualizationSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new visualization section heading"
            it.visualization shouldBe ResourceReference(ThingId("R215648"), "Some visualization", setOf(Classes.visualization))
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", smartReviewId, sectionId)
            .content(updateVisualizationSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(smartReviewId)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewVisualizationSection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated visualization section heading"
            it.visualization shouldBe ResourceReference(ThingId("R2215648"), "Some other visualization", setOf(Classes.visualization))
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateResourceSection() {
        val id = createSmartReview()

        post("/api/smart-reviews/$id/sections")
            .content(createResourceSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewResourceSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new resource section heading"
            it.resource shouldBe ResourceReference(ThingId("R14565"), "Some dataset resource", setOf(Classes.dataset))
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(updateResourceSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewResourceSection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated resource section heading"
            it.resource shouldBe ResourceReference(ThingId("R214565"), "Some other dataset resource", setOf(Classes.dataset))
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdatePredicateSection() {
        val id = createSmartReview()

        post("/api/smart-reviews/$id/sections")
            .content(createPredicateSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewPredicateSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new predicate section heading"
            it.predicate shouldBe PredicateReference(ThingId("R15696541"), "Some predicate")
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(updatePredicateSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewPredicateSection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated predicate section heading"
            it.predicate shouldBe PredicateReference(ThingId("R215696541"), "Some other predicate")
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateOntologySection() {
        val id = createSmartReview()

        post("/api/smart-reviews/$id/sections")
            .content(createOntologySectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewOntologySection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new ontology section heading"
            it.entities shouldBe listOf(
                ResourceReference(ThingId("R1"), "Some ontology resource", emptySet()),
                PredicateReference(ThingId("P1"), "Some ontology predicate")
            )
            it.predicates shouldBe listOf(PredicateReference(ThingId("P1"), "Some ontology predicate"))
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(updateOntologySectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewOntologySection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated ontology section heading"
            it.entities shouldBe listOf(
                ResourceReference(ThingId("R21"), "Some other ontology resource", emptySet()),
                PredicateReference(ThingId("P1"), "Some ontology predicate")
            )
            it.predicates shouldBe listOf(PredicateReference(ThingId("P21"), "Some other ontology predicate"))
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateTextSection() {
        val id = createSmartReview()

        post("/api/smart-reviews/$id/sections")
            .content(createTextSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val smartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        smartReview.sections.size shouldBe 7
        smartReview.sections.last().shouldBeInstanceOf<SmartReviewTextSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "new text section heading"
            it.classes shouldBe setOf(Classes.epilogue)
            it.text shouldBe "Epilogue text section contents"
        }

        val sectionId = smartReview.sections.last().id

        put("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(updateTextSectionJson)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedSmartReview = smartReviewService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedSmartReview.sections.last().shouldBeInstanceOf<SmartReviewTextSection>().asClue {
            it.id shouldBe sectionId
            it.heading shouldBe "updated text section heading"
            it.classes shouldBe setOf(Classes.introduction)
            it.text shouldBe "Introduction text section contents"
        }
    }

    private fun createSmartReview() = post("/api/smart-reviews")
        .content(createSmartReviewJson)
        .accept(SMART_REVIEW_JSON_V1)
        .contentType(SMART_REVIEW_JSON_V1)
        .perform()
        .andExpect(status().isCreated)
        .andReturn()
        .response
        .getHeaderValue("Location")!!
        .toString()
        .substringAfterLast("/")
        .let(::ThingId)
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
      "homepage": "https://example.org/author"
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

private const val updateSmartReviewJson = """{
  "title": "updated smart review title",
  "research_fields": [
    "R194"
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
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "extraction_method": "UNKNOWN",
  "sections": [
    {
      "heading": "updated comparison section heading",
      "comparison": "R26416"
    },
    {
      "heading": "updated visualization section heading",
      "visualization": "R2215648"
    },
    {
      "heading": "updated resource section heading",
      "resource": "R214565"
    },
    {
      "heading": "updated predicate section heading",
      "predicate": "R215696541"
    },
    {
      "heading": "updated ontology section heading",
      "entities": ["R21", "P1"],
      "predicates": ["P21"]
    },
    {
      "heading": "updated text section heading",
      "class": "Introduction",
      "text": "Introduction text section contents"
    }
  ],
  "references": [
    "@misc{R615465, title = {updated reference 1}}",
    "@misc{R154146, title = {updated reference 2}}"
  ]
}"""

private const val createComparisonSectionJson = """{
  "heading": "new comparison section heading",
  "comparison": "R6416"
}"""

private const val createVisualizationSectionJson = """{
  "heading": "new visualization section heading",
  "visualization": "R215648"
}"""

private const val createResourceSectionJson = """{
  "heading": "new resource section heading",
  "resource": "R14565"
}"""

private const val createPredicateSectionJson = """{
  "heading": "new predicate section heading",
  "predicate": "R15696541"
}"""

private const val createOntologySectionJson = """{
  "heading": "new ontology section heading",
  "entities": ["R1", "P1"],
  "predicates": ["P1"]
}"""

private const val createTextSectionJson = """{
  "heading": "new text section heading",
  "class": "Epilogue",
  "text": "Epilogue text section contents"
}"""

private const val updateComparisonSectionJson = """{
  "heading": "updated comparison section heading",
  "comparison": "R26416"
}"""

private const val updateVisualizationSectionJson = """{
  "heading": "updated visualization section heading",
  "visualization": "R2215648"
}"""

private const val updateResourceSectionJson = """{
  "heading": "updated resource section heading",
  "resource": "R214565"
}"""

private const val updatePredicateSectionJson = """{
  "heading": "updated predicate section heading",
  "predicate": "R215696541"
}"""

private const val updateOntologySectionJson = """{
  "heading": "updated ontology section heading",
  "entities": ["R21", "P1"],
  "predicates": ["P21"]
}"""

private const val updateTextSectionJson = """{
  "heading": "updated text section heading",
  "class": "Introduction",
  "text": "Introduction text section contents"
}"""
