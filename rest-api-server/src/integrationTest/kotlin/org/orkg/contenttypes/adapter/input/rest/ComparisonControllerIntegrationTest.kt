@file:Suppress("HttpUrlsUsage")

package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.adapter.output.simcomp.internal.InMemorySimCompThingRepositoryAdapter
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.input.ComparisonUseCases
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
import org.orkg.testing.annotations.IntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@IntegrationTest
@Import(InMemorySimCompThingRepositoryAdapter::class)
internal class ComparisonControllerIntegrationTest : MockMvcBaseTest("comparisons") {
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
    private lateinit var comparisonService: ComparisonUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.findAll()).hasSize(0)
        assertThat(organizationService.findAllConferences()).hasSize(0)

        predicateService.createPredicates(
            Predicates.hasAuthors,
            Predicates.hasSubject,
            Predicates.comparesContribution,
            Predicates.hasResearchProblem,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.reference,
            Predicates.isAnonymized,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement,
            Predicates.sustainableDevelopmentGoal,
            Predicates.hasVisualization,
        )

        classService.createClasses(
            Classes.contribution,
            Classes.problem,
            Classes.researchField,
            Classes.author,
            Classes.venue,
            Classes.sustainableDevelopmentGoal,
            Classes.visualization,
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField),
        )

        resourceService.createResource(
            id = ThingId("R13"),
            label = "Engineering",
            classes = setOf(Classes.researchField),
        )

        // Example specific entities

        classService.createClasses(ThingId("Result"))

        resourceService.createResource(id = ThingId("R6541"), label = "Contribution 1", classes = setOf(Classes.contribution))
        resourceService.createResource(id = ThingId("R5364"), label = "Contribution 2", classes = setOf(Classes.contribution))
        resourceService.createResource(id = ThingId("R9786"), label = "Contribution 3", classes = setOf(Classes.contribution))
        resourceService.createResource(id = ThingId("R3120"), label = "Contribution 4", classes = setOf(Classes.contribution))
        resourceService.createResource(id = ThingId("R7864"), label = "Contribution 5", classes = setOf(Classes.contribution))

        resourceService.createResource(id = ThingId("R6571"), label = "Visualization 1", classes = setOf(Classes.visualization))
        resourceService.createResource(id = ThingId("R1354"), label = "Visualization 2", classes = setOf(Classes.visualization))

        resourceService.createResource(id = ThingId("R123"), label = "Author with id", classes = setOf(Classes.author))
        resourceService.createResource(id = ThingId("R124"), label = "Other author with id", classes = setOf(Classes.author))

        resourceService.createResource(id = ThingId("SDG_1"), label = "No poverty", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_2"), label = "Zero hunger", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_3"), label = "Good health and well-being", classes = setOf(Classes.sustainableDevelopmentGoal))

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R456"),
                label = "Author with id and orcid",
                classes = setOf(Classes.author),
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "1111-2222-3333-4444"),
        )

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R4567"),
                label = "Author with orcid",
                classes = setOf(Classes.author),
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "0000-1111-2222-3333"),
        )

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            createdBy = contributorId,
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
        )
        organizationService.createOrganization(
            createdBy = contributorId,
            id = OrganizationId("dc9a860c-1a1b-4977-bd6a-9dc21de46df6"),
            organizationName = "Different Organization", // required to satisfy unique constraint
        )

        observatoryService.createObservatory(
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
            researchField = ThingId("R12"),
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"),
        )
        observatoryService.createObservatory(
            organizations = setOf(OrganizationId("dc9a860c-1a1b-4977-bd6a-9dc21de46df6")),
            researchField = ThingId("R12"),
            id = ObservatoryId("33d0776f-59ad-465f-a22c-cd794694edc6"),
            name = "Different Observatory", // required to satisfy unique constraint
        )
    }

    @AfterEach
    fun cleanup() {
        statementService.deleteAll()
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
        val id = post("/api/comparisons")
            .content(requestJson("orkg/createComparison"))
            .accept(COMPARISON_JSON_V3)
            .contentType(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val comparison = get("/api/comparisons/{id}", id)
            .accept(COMPARISON_JSON_V3)
            .contentType(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, ComparisonRepresentation::class.java) }

        comparison.asClue {
            it.id shouldBe id
            it.title shouldBe "example comparison"
            it.researchFields shouldBe listOf(
                LabeledObjectRepresentation(ThingId("R12"), "Computer Science"),
            )
            it.authors.size shouldBe 5
            it.authors[0] shouldBe AuthorRepresentation(
                name = "Author with id",
                id = ThingId("R123"),
                identifiers = emptyMap(),
                homepage = null,
            )
            it.authors[1] shouldBe AuthorRepresentation(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null,
            )
            it.authors[2] shouldBe AuthorRepresentation(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                homepage = null,
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe IRI.create("https://example.org/author")
            }
            it.authors[4] shouldBe AuthorRepresentation(
                name = "Author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null,
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                LabeledObjectRepresentation(ThingId("SDG_1"), "No poverty"),
                LabeledObjectRepresentation(ThingId("SDG_2"), "Zero hunger"),
            )
            it.sources shouldBe listOf(
                ComparisonDataSourceRepresentation(ThingId("R6541"), ComparisonDataSource.Type.THING),
                ComparisonDataSourceRepresentation(ThingId("R5364"), ComparisonDataSource.Type.THING),
                ComparisonDataSourceRepresentation(ThingId("R9786"), ComparisonDataSource.Type.THING),
                ComparisonDataSourceRepresentation(ThingId("R3120"), ComparisonDataSource.Type.THING),
            )
            it.visualizations shouldContainExactlyInAnyOrder listOf(
                LabeledObjectRepresentation(ThingId("R6571"), "Visualization 1"),
                LabeledObjectRepresentation(ThingId("R1354"), "Visualization 2"),
            )
            it.relatedFigures shouldBe emptyList()
            it.relatedResources shouldBe emptyList()
            it.references shouldContainExactly listOf("https://orkg.org/resources/R1000", "paper citation")
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.isAnonymized shouldBe false
            it.visibility shouldBe Visibility.DEFAULT
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.versions shouldBe VersionInfoRepresentation(
                head = HeadVersionRepresentation(it.id, it.title, it.createdAt, it.createdBy),
                published = emptyList(),
            )
            it.published shouldBe false
            it.unlistedBy shouldBe null
        }

        put("/api/comparisons/{id}", id)
            .content(requestJson("orkg/updateComparison"))
            .accept(COMPARISON_JSON_V3)
            .contentType(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isNoContent)

        val updatedComparison = comparisonService.findById(id).orElseThrow { ComparisonNotFound(id) }

        updatedComparison.asClue {
            it.id shouldBe id
            it.title shouldBe "updated comparison"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R13"), "Engineering"),
            )
            it.authors.size shouldBe 5
            it.authors[0] shouldBe Author(
                name = "Other author with id",
                id = ThingId("R124"),
                identifiers = emptyMap(),
                homepage = null,
            )
            it.authors[1] shouldBe Author(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null,
            )
            it.authors[2] shouldBe Author(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                homepage = null,
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe IRI.create("https://example.org/author")
            }
            it.authors[4] shouldBe Author(
                name = "Author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null,
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_2"), "Zero hunger"),
                ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"),
            )
            it.sources shouldBe listOf(
                ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.THING),
                ComparisonDataSource(ThingId("R5364"), ComparisonDataSource.Type.THING),
                ComparisonDataSource(ThingId("R3120"), ComparisonDataSource.Type.THING),
                ComparisonDataSource(ThingId("R7864"), ComparisonDataSource.Type.THING),
            )
            it.visualizations shouldContainExactlyInAnyOrder listOf(
                ObjectIdAndLabel(ThingId("R1354"), "Visualization 2"),
                ObjectIdAndLabel(ThingId("R6571"), "Visualization 1"),
            )
            it.relatedFigures shouldBe emptyList()
            it.relatedResources shouldBe emptyList()
            it.references shouldContainExactly listOf("other paper citation", "paper citation")
            it.observatories shouldBe listOf(ObservatoryId("33d0776f-59ad-465f-a22c-cd794694edc6"))
            it.organizations shouldBe listOf(OrganizationId("dc9a860c-1a1b-4977-bd6a-9dc21de46df6"))
            it.isAnonymized shouldBe true
            it.visibility shouldBe Visibility.DELETED
            it.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.versions shouldBe VersionInfo(
                head = HeadVersion(it.id, it.title, it.createdAt, it.createdBy),
                published = emptyList(),
            )
            it.published shouldBe false
            it.unlistedBy shouldBe null
        }
    }
}
