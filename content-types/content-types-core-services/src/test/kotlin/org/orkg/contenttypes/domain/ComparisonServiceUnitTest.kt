package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.output.ConferenceSeriesRepository
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonConfig
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonData
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class ComparisonServiceUnitTest : MockkBaseTest {
    private val contributionComparisonRepository: ContributionComparisonRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val listRepository: ListRepository = mockk()
    private val doiService: DoiService = mockk()
    private val conferenceSeriesRepository: ConferenceSeriesRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val comparisonRepository: ComparisonRepository = mockk()
    private val comparisonTableRepository: ComparisonTableRepository = mockk()
    private val comparisonPublishedRepository: ComparisonPublishedRepository = mockk()

    private val service = ComparisonService(
        repository = contributionComparisonRepository,
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        unsafeResourceUseCases = unsafeResourceUseCases,
        statementService = statementService,
        unsafeStatementUseCases = unsafeStatementUseCases,
        unsafeLiteralUseCases = unsafeLiteralUseCases,
        listService = listService,
        listRepository = listRepository,
        doiService = doiService,
        conferenceSeriesRepository = conferenceSeriesRepository,
        contributorRepository = contributorRepository,
        comparisonRepository = comparisonRepository,
        comparisonTableRepository = comparisonTableRepository,
        comparisonPublishedRepository = comparisonPublishedRepository,
        comparisonPublishBaseUri = "https://orkg.org/comparison/"
    )

    @Test
    fun `Given an unpublished comparison, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.comparison),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val publishedVersion = createResource(
            id = ThingId("R156"),
            label = "published comparison",
            classes = setOf(Classes.comparisonPublished),
            createdAt = OffsetDateTime.now(fixedClock).minusDays(1),
            createdBy = ContributorId(MockUserId.USER)
        )
        val versions = VersionInfo(
            head = HeadVersion(expected),
            published = listOf(PublishedVersion(publishedVersion, null))
        )
        val researchFieldId = ThingId("R20")
        val doi = "10.1000/182"
        val publicationYear: Long = 2016
        val publicationMonth = 1
        val reference = "https://orkg.org"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val firstBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(
                Classes.researchField,
                Classes.contribution,
                Classes.visualization,
                Classes.comparisonRelatedFigure,
                Classes.comparisonRelatedResource,
                Classes.sustainableDevelopmentGoal
            ),
            whitelist = emptyList()
        )
        val secondBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 1,
            blacklist = emptyList(),
            whitelist = listOf(
                Classes.researchField,
                Classes.contribution,
                Classes.visualization,
                Classes.comparisonRelatedFigure,
                Classes.comparisonRelatedResource,
                Classes.sustainableDevelopmentGoal
            )
        )
        val config = createComparisonConfig()
        val data = createComparisonData()

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = firstBundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasDOI),
                `object` = createLiteral(label = doi)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.yearPublished),
                `object` = createLiteral(
                    label = publicationYear.toString(),
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.monthPublished),
                `object` = createLiteral(label = publicationMonth.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.reference),
                `object` = createLiteral(label = reference)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.isAnonymized),
                `object` = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
            ),
            createStatement(
                subject = authorList,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasPublishedVersion),
                `object` = publishedVersion
            )
        )
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = secondBundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.sustainableDevelopmentGoal),
                `object` = createResource(
                    classes = setOf(Classes.sustainableDevelopmentGoal),
                    label = "No poverty",
                    id = ThingId("SDG_1")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasSubject),
                `object` = createResource(
                    id = researchFieldId,
                    classes = setOf(Classes.researchField),
                    label = "Research Field 1"
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.comparesContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasVisualization),
                `object` = createResource(
                    classes = setOf(Classes.visualization),
                    label = "Visualization",
                    id = ThingId("Visualization123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedResource),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedResource),
                    label = "ComparisonRelatedResource",
                    id = ThingId("ComparisonRelatedResource123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedFigure),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedFigure),
                    label = "ComparisonRelatedFigure",
                    id = ThingId("ComparisonRelatedFigure123")
                )
            )
        )
        every { comparisonTableRepository.findById(expected.id) } returns Optional.of(ComparisonTable(expected.id, config, data))

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get().asClue { comparison ->
            comparison.id shouldBe expected.id
            comparison.title shouldBe expected.label
            comparison.researchFields shouldNotBe null
            comparison.researchFields shouldBe listOf(
                ObjectIdAndLabel(researchFieldId, "Research Field 1")
            )
            comparison.identifiers shouldNotBe null
            comparison.identifiers shouldBe mapOf(
                "doi" to listOf(doi)
            )
            comparison.publicationInfo shouldNotBe null
            comparison.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe publicationMonth
                publicationInfo.publishedYear shouldBe publicationYear
                publicationInfo.publishedIn shouldBe null
                publicationInfo.url shouldBe null
            }
            comparison.authors shouldNotBe null
            comparison.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                )
            )
            comparison.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_1"), "No poverty")
            )
            comparison.contributions shouldNotBe null
            comparison.contributions shouldBe listOf(
                ObjectIdAndLabel(ThingId("Contribution123"), "Contribution")
            )
            comparison.config shouldBe config
            comparison.data shouldBe data
            comparison.visualizations shouldNotBe null
            comparison.visualizations shouldBe listOf(
                ObjectIdAndLabel(ThingId("Visualization123"), "Visualization")
            )
            comparison.relatedFigures shouldNotBe null
            comparison.relatedFigures shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedFigure123"), "ComparisonRelatedFigure")
            )
            comparison.relatedResources shouldNotBe null
            comparison.relatedResources shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedResource123"), "ComparisonRelatedResource")
            )
            comparison.references shouldNotBe null
            comparison.references shouldBe listOf(reference)
            comparison.observatories shouldBe setOf(expected.observatoryId)
            comparison.organizations shouldBe setOf(expected.organizationId)
            comparison.extractionMethod shouldBe expected.extractionMethod
            comparison.createdAt shouldBe expected.createdAt
            comparison.createdBy shouldBe expected.createdBy
            comparison.versions shouldBe versions
            comparison.isAnonymized shouldBe true
            comparison.visibility shouldBe Visibility.DEFAULT
            comparison.published shouldBe false
            comparison.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = firstBundleConfiguration,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = secondBundleConfiguration,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) { comparisonTableRepository.findById(expected.id) }
    }

    @Test
    fun `Given a published comparison, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.comparisonPublished),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val otherPublishedVersion = createResource(
            id = ThingId("R6481"),
            label = "published comparison",
            classes = setOf(Classes.comparisonPublished),
            createdAt = OffsetDateTime.now(fixedClock).minusDays(2),
            createdBy = ContributorId(MockUserId.USER)
        )
        val headVersion = createResource(
            id = ThingId("R5346"),
            label = "head version comparison",
            classes = setOf(Classes.comparison),
            createdAt = OffsetDateTime.now(fixedClock).minusDays(3),
            createdBy = ContributorId(MockUserId.USER)
        )
        val versions = VersionInfo(
            head = HeadVersion(headVersion),
            published = listOf(
                PublishedVersion(expected, null),
                PublishedVersion(otherPublishedVersion, null)
            )
        )
        val researchFieldId = ThingId("R20")
        val doi = "10.1000/182"
        val publicationYear: Long = 2016
        val publicationMonth = 1
        val reference = "https://orkg.org"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val firstBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(
                Classes.researchField,
                Classes.contribution,
                Classes.visualization,
                Classes.comparisonRelatedFigure,
                Classes.comparisonRelatedResource,
                Classes.sustainableDevelopmentGoal
            ),
            whitelist = emptyList()
        )
        val secondBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 1,
            blacklist = emptyList(),
            whitelist = listOf(
                Classes.researchField,
                Classes.contribution,
                Classes.visualization,
                Classes.comparisonRelatedFigure,
                Classes.comparisonRelatedResource,
                Classes.sustainableDevelopmentGoal
            )
        )
        val config = createComparisonConfig()
        val data = createComparisonData()

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = firstBundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasDOI),
                `object` = createLiteral(label = doi)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.yearPublished),
                `object` = createLiteral(
                    label = publicationYear.toString(),
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.monthPublished),
                `object` = createLiteral(label = publicationMonth.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.reference),
                `object` = createLiteral(label = reference)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.isAnonymized),
                `object` = createLiteral(label = "true", datatype = Literals.XSD.BOOLEAN.prefixedUri)
            ),
            createStatement(
                subject = authorList,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            )
        )
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = secondBundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.sustainableDevelopmentGoal),
                `object` = createResource(
                    classes = setOf(Classes.sustainableDevelopmentGoal),
                    label = "No poverty",
                    id = ThingId("SDG_1")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasSubject),
                `object` = createResource(
                    id = researchFieldId,
                    classes = setOf(Classes.researchField),
                    label = "Research Field 1"
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.comparesContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasVisualization),
                `object` = createResource(
                    classes = setOf(Classes.visualization),
                    label = "Visualization",
                    id = ThingId("Visualization123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedResource),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedResource),
                    label = "ComparisonRelatedResource",
                    id = ThingId("ComparisonRelatedResource123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedFigure),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedFigure),
                    label = "ComparisonRelatedFigure",
                    id = ThingId("ComparisonRelatedFigure123")
                )
            )
        )
        every {
            comparisonRepository.findVersionHistoryForPublishedComparison(expected.id)
        } returns versions
        every { comparisonPublishedRepository.findById(expected.id) } returns Optional.of(PublishedComparison(expected.id, config, data))

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get().asClue { comparison ->
            comparison.id shouldBe expected.id
            comparison.title shouldBe expected.label
            comparison.researchFields shouldNotBe null
            comparison.researchFields shouldBe listOf(
                ObjectIdAndLabel(researchFieldId, "Research Field 1")
            )
            comparison.identifiers shouldNotBe null
            comparison.identifiers shouldBe mapOf(
                "doi" to listOf(doi)
            )
            comparison.publicationInfo shouldNotBe null
            comparison.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe publicationMonth
                publicationInfo.publishedYear shouldBe publicationYear
                publicationInfo.publishedIn shouldBe null
                publicationInfo.url shouldBe null
            }
            comparison.authors shouldNotBe null
            comparison.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                )
            )
            comparison.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_1"), "No poverty")
            )
            comparison.contributions shouldNotBe null
            comparison.contributions shouldBe listOf(
                ObjectIdAndLabel(ThingId("Contribution123"), "Contribution")
            )
            comparison.config shouldBe config
            comparison.data shouldBe data
            comparison.visualizations shouldNotBe null
            comparison.visualizations shouldBe listOf(
                ObjectIdAndLabel(ThingId("Visualization123"), "Visualization")
            )
            comparison.relatedFigures shouldNotBe null
            comparison.relatedFigures shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedFigure123"), "ComparisonRelatedFigure")
            )
            comparison.relatedResources shouldNotBe null
            comparison.relatedResources shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedResource123"), "ComparisonRelatedResource")
            )
            comparison.references shouldNotBe null
            comparison.references shouldBe listOf(reference)
            comparison.observatories shouldBe setOf(expected.observatoryId)
            comparison.organizations shouldBe setOf(expected.organizationId)
            comparison.extractionMethod shouldBe expected.extractionMethod
            comparison.createdAt shouldBe expected.createdAt
            comparison.createdBy shouldBe expected.createdBy
            comparison.versions shouldBe versions
            comparison.isAnonymized shouldBe true
            comparison.visibility shouldBe Visibility.DEFAULT
            comparison.published shouldBe true
            comparison.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = firstBundleConfiguration,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = secondBundleConfiguration,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            comparisonRepository.findVersionHistoryForPublishedComparison(expected.id)
        }
        verify(exactly = 1) { comparisonPublishedRepository.findById(expected.id) }
    }
}
