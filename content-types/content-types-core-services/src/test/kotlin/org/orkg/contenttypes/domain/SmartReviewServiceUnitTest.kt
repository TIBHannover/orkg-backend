package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class SmartReviewServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val smartReviewRepository: SmartReviewRepository = mockk()
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository = mockk()
    private val comparisonService: ComparisonService = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val listRepository: ListRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val doiService: DoiService = mockk()

    private val service = SmartReviewService(
        resourceRepository = resourceRepository,
        smartReviewRepository = smartReviewRepository,
        smartReviewPublishedRepository = smartReviewPublishedRepository,
        comparisonService = comparisonService,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        predicateRepository = predicateRepository,
        thingRepository = thingRepository,
        resourceService = resourceService,
        unsafeResourceUseCases = unsafeResourceUseCases,
        unsafeLiteralUseCases = unsafeLiteralUseCases,
        statementService = statementService,
        unsafeStatementUseCases = unsafeStatementUseCases,
        listService = listService,
        listRepository = listRepository,
        contributorRepository = contributorRepository,
        doiService = doiService,
        smartReviewPublishBaseUri = "https://orkg.org/review/"
    )

    @Test
    fun `Given an unpublished smart review, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.smartReview),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val researchFieldId = ThingId("R20")
        val publishedVersion1 = createResource(
            id = ThingId("R235467"),
            label = "published1",
            classes = setOf(Classes.smartReviewPublished)
        )
        val changelog1 = createLiteral(id = ThingId("L13546"), label = "changelog1")
        val publishedVersion2 = createResource(
            id = ThingId("R154687"),
            label = "published2",
            classes = setOf(Classes.smartReviewPublished)
        )
        val changelog2 = createLiteral(id = ThingId("L5753"), label = "changelog2")
        val contribution = createResource(id = ThingId("R4596"), classes = setOf(Classes.contributionSmartReview))
        val comparisonSection = createResource(
            id = ThingId("R963"),
            label = "comparison section heading",
            classes = setOf(Classes.comparisonSection)
        )
        val comparison = createResource(id = ThingId("R1546878"), classes = setOf(Classes.comparison))
        val visualizationSection = createResource(
            id = ThingId("R852"),
            label = "visualization section heading",
            classes = setOf(Classes.visualizationSection)
        )
        val visualization = createResource(id = ThingId("R31256"), classes = setOf(Classes.visualization))
        val resourceSection = createResource(
            id = ThingId("R741"),
            label = "resource section heading",
            classes = setOf(Classes.resourceSection)
        )
        val resource = createResource(id = ThingId("R5641"), classes = setOf(Classes.problem))
        val propertySection = createResource(
            id = ThingId("R852"),
            label = "property section heading",
            classes = setOf(Classes.propertySection)
        )
        val property = createPredicate(id = ThingId("P7689"))
        val ontologySection = createResource(
            id = ThingId("R159"),
            label = "ontology section heading",
            classes = setOf(Classes.ontologySection)
        )
        val ontologyResourceEntity = createResource(id = ThingId("R13457"), classes = setOf(Classes.author))
        val ontologyPredicateEntity = createPredicate(id = ThingId("P6148"))
        val ontologyPredicate = createPredicate(id = ThingId("P73489"))
        val textSection = createResource(id = ThingId("R489"), label = "heading", classes = setOf(Classes.section, Classes.introduction))
        val textContent = "text content"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val resourceAuthor = createResource(id = ThingId("R132564"), label = "Author 2", classes = setOf(Classes.author))
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(Classes.researchField, Classes.venue),
            whitelist = emptyList()
        )

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
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
                predicate = createPredicate(Predicates.hasPublishedVersion),
                `object` = publishedVersion1,
                createdAt = OffsetDateTime.now(fixedClock).minusDays(2)
            ),
            createStatement(
                subject = publishedVersion1,
                predicate = createPredicate(Predicates.description),
                `object` = changelog1
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasPublishedVersion),
                `object` = publishedVersion2,
                createdAt = OffsetDateTime.now(fixedClock).minusDays(1)
            ),
            createStatement(
                subject = publishedVersion2,
                predicate = createPredicate(Predicates.description),
                `object` = changelog2
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasContribution),
                `object` = contribution
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = textSection
            ),
            createStatement(
                subject = textSection,
                predicate = createPredicate(Predicates.hasContent),
                `object` = createLiteral(label = textContent)
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = comparisonSection
            ),
            createStatement(
                subject = comparisonSection,
                predicate = createPredicate(Predicates.hasLink),
                `object` = comparison
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = visualizationSection
            ),
            createStatement(
                subject = visualizationSection,
                predicate = createPredicate(Predicates.hasLink),
                `object` = visualization
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = resourceSection
            ),
            createStatement(
                subject = resourceSection,
                predicate = createPredicate(Predicates.hasLink),
                `object` = resource
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = propertySection
            ),
            createStatement(
                subject = propertySection,
                predicate = createPredicate(Predicates.hasLink),
                `object` = property
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasSection),
                `object` = ontologySection
            ),
            createStatement(
                subject = ontologySection,
                predicate = createPredicate(Predicates.hasEntity),
                `object` = ontologyResourceEntity
            ),
            createStatement(
                subject = ontologySection,
                predicate = createPredicate(Predicates.hasEntity),
                `object` = ontologyPredicateEntity
            ),
            createStatement(
                subject = ontologySection,
                predicate = createPredicate(Predicates.showProperty),
                `object` = ontologyPredicate
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = authorList,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            ),
            createStatement(
                subject = authorList,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = resourceAuthor
            ),
            createStatement(
                subject = resourceAuthor,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            ),
            createStatement(
                subject = resourceAuthor,
                predicate = createPredicate(Predicates.hasWebsite),
                `object` = createLiteral(label = "https://example.org", datatype = Literals.XSD.URI.prefixedUri)
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasReference),
                `object` = createLiteral(label = "reference 1")
            ),
            createStatement(
                subject = contribution,
                predicate = createPredicate(Predicates.hasReference),
                `object` = createLiteral(label = "reference 2")
            )
        )
        every {
            statementRepository.findAll(
                subjectId = expected.id,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasResearchField),
                `object` = createResource(
                    id = researchFieldId,
                    classes = setOf(Classes.researchField),
                    label = "Research Field 1"
                )
            )
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue {
            it.id shouldBe expected.id
            it.title shouldBe expected.label
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(id = researchFieldId, label = "Research Field 1")
            )
            it.authors shouldNotBe null
            it.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                ),
                Author(
                    id = resourceAuthor.id,
                    name = "Author 2",
                    identifiers = mapOf(
                        "orcid" to listOf("0000-1111-2222-3333")
                    ),
                    homepage = ParsedIRI("https://example.org")
                )
            )
            it.versions shouldBe VersionInfo(
                head = HeadVersion(expected),
                published = listOf(
                    PublishedVersion(publishedVersion2, changelog2.label),
                    PublishedVersion(publishedVersion1, changelog1.label)
                )
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_1"), "No poverty")
            )
            it.observatories shouldBe setOf(expected.observatoryId)
            it.organizations shouldBe setOf(expected.organizationId)
            it.extractionMethod shouldBe expected.extractionMethod
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe expected.unlistedBy
            it.published shouldBe false
            it.sections shouldBe listOf(
                SmartReviewTextSection(textSection.id, textSection.label, setOf(Classes.introduction), textContent),
                SmartReviewComparisonSection(comparisonSection.id, "comparison section heading", ResourceReference(comparison)),
                SmartReviewVisualizationSection(visualizationSection.id, "visualization section heading", ResourceReference(visualization)),
                SmartReviewResourceSection(resourceSection.id, "resource section heading", ResourceReference(resource)),
                SmartReviewPredicateSection(propertySection.id, "property section heading", PredicateReference(property)),
                SmartReviewOntologySection(
                    id = ontologySection.id,
                    heading = "ontology section heading",
                    entities = listOf(ResourceReference(ontologyResourceEntity), PredicateReference(ontologyPredicateEntity)),
                    predicates = listOf(PredicateReference(ontologyPredicate))
                )
            )
            it.references shouldBe listOf("reference 1", "reference 2")
            it.acknowledgements shouldBe mapOf(
                ContributorId.UNKNOWN to 0.875,
                ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c") to 0.125
            )
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = expected.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = expected.id,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a published smart review, when fetching it by id, then it is returned`() {
        val unpublished = createResource(
            classes = setOf(Classes.smartReview),
        )
        val publishedVersion1 = createResource(
            id = ThingId("R235467"),
            label = "published1",
            classes = setOf(Classes.smartReviewPublished),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val changelog1 = createLiteral(id = ThingId("L13546"), label = "changelog1")

        val expected = createResource(
            id = ThingId("R154687"),
            label = "published2",
            classes = setOf(Classes.smartReviewPublished)
        )
        val changelog = createLiteral(id = ThingId("L5753"), label = "changelog2")

        val researchFieldId = ThingId("R20")
        val contribution = createResource(id = ThingId("R4596"), classes = setOf(Classes.contributionSmartReview))
        val comparisonSection = createResource(
            id = ThingId("R963"),
            label = "comparison section heading",
            classes = setOf(Classes.comparisonSection)
        )
        val comparison = createResource(id = ThingId("R1546878"), classes = setOf(Classes.comparison))
        val visualizationSection = createResource(
            id = ThingId("R852"),
            label = "visualization section heading",
            classes = setOf(Classes.visualizationSection)
        )
        val visualization = createResource(id = ThingId("R31256"), classes = setOf(Classes.visualization))
        val resourceSection = createResource(
            id = ThingId("R741"),
            label = "resource section heading",
            classes = setOf(Classes.resourceSection)
        )
        val resource = createResource(id = ThingId("R5641"), classes = setOf(Classes.problem))
        val propertySection = createResource(
            id = ThingId("R852"),
            label = "property section heading",
            classes = setOf(Classes.propertySection)
        )
        val property = createPredicate(id = ThingId("P7689"))
        val ontologySection = createResource(
            id = ThingId("R159"),
            label = "ontology section heading",
            classes = setOf(Classes.ontologySection)
        )
        val ontologyResourceEntity = createResource(id = ThingId("R13457"), classes = setOf(Classes.author))
        val ontologyPredicateEntity = createPredicate(id = ThingId("P6148"))
        val ontologyPredicate = createPredicate(id = ThingId("P73489"))
        val textSection = createResource(id = ThingId("R489"), label = "heading", classes = setOf(Classes.section, Classes.introduction))
        val textContent = "text content"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val resourceAuthor = createResource(id = ThingId("R132564"), label = "Author 2", classes = setOf(Classes.author))
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 2,
            blacklist = emptyList(),
            whitelist = listOf(Classes.smartReview, Classes.smartReviewPublished, Classes.literal)
        )

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { smartReviewPublishedRepository.findById(expected.id) } returns Optional.of(
            PublishedContentType(
                id = expected.id,
                rootId = unpublished.id,
                subgraph = listOf(
                    createStatement(
                        subject = unpublished,
                        predicate = createPredicate(Predicates.sustainableDevelopmentGoal),
                        `object` = createResource(
                            classes = setOf(Classes.sustainableDevelopmentGoal),
                            label = "No poverty",
                            id = ThingId("SDG_1")
                        )
                    ),
                    createStatement(
                        subject = unpublished,
                        predicate = createPredicate(Predicates.hasResearchField),
                        `object` = createResource(
                            id = researchFieldId,
                            classes = setOf(Classes.researchField),
                            label = "Research Field 1"
                        )
                    ),
                    createStatement(
                        subject = unpublished,
                        predicate = createPredicate(Predicates.hasContribution),
                        `object` = contribution
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = textSection
                    ),
                    createStatement(
                        subject = textSection,
                        predicate = createPredicate(Predicates.hasContent),
                        `object` = createLiteral(label = textContent)
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = comparisonSection
                    ),
                    createStatement(
                        subject = comparisonSection,
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = comparison
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = visualizationSection
                    ),
                    createStatement(
                        subject = visualizationSection,
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = visualization
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = resourceSection
                    ),
                    createStatement(
                        subject = resourceSection,
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = resource
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = propertySection
                    ),
                    createStatement(
                        subject = propertySection,
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = property
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = ontologySection
                    ),
                    createStatement(
                        subject = ontologySection,
                        predicate = createPredicate(Predicates.hasEntity),
                        `object` = ontologyResourceEntity
                    ),
                    createStatement(
                        subject = ontologySection,
                        predicate = createPredicate(Predicates.hasEntity),
                        `object` = ontologyPredicateEntity
                    ),
                    createStatement(
                        subject = ontologySection,
                        predicate = createPredicate(Predicates.showProperty),
                        `object` = ontologyPredicate
                    ),
                    createStatement(
                        subject = unpublished,
                        predicate = createPredicate(Predicates.hasAuthors),
                        `object` = authorList
                    ),
                    createStatement(
                        subject = authorList,
                        predicate = createPredicate(Predicates.hasListElement),
                        `object` = createLiteral(label = "Author 1")
                    ),
                    createStatement(
                        subject = authorList,
                        predicate = createPredicate(Predicates.hasListElement),
                        `object` = resourceAuthor
                    ),
                    createStatement(
                        subject = resourceAuthor,
                        predicate = createPredicate(Predicates.hasORCID),
                        `object` = createLiteral(label = "0000-1111-2222-3333")
                    ),
                    createStatement(
                        subject = resourceAuthor,
                        predicate = createPredicate(Predicates.hasWebsite),
                        `object` = createLiteral(label = "https://example.org", datatype = Literals.XSD.URI.prefixedUri)
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasReference),
                        `object` = createLiteral(label = "reference 1")
                    ),
                    createStatement(
                        subject = contribution,
                        predicate = createPredicate(Predicates.hasReference),
                        `object` = createLiteral(label = "reference 2")
                    )
                )
            )
        )

        every {
            statementRepository.fetchAsBundle(
                id = unpublished.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        } returns pageOf(
            createStatement(
                subject = unpublished,
                predicate = createPredicate(Predicates.hasPublishedVersion),
                `object` = publishedVersion1,
                createdAt = OffsetDateTime.now(fixedClock).minusDays(2)
            ),
            createStatement(
                subject = publishedVersion1,
                predicate = createPredicate(Predicates.description),
                `object` = changelog1
            ),
            createStatement(
                subject = unpublished,
                predicate = createPredicate(Predicates.hasPublishedVersion),
                `object` = expected,
                createdAt = OffsetDateTime.now(fixedClock).minusDays(1)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.description),
                `object` = changelog
            )
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue {
            it.id shouldBe expected.id
            it.title shouldBe expected.label
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(id = researchFieldId, label = "Research Field 1")
            )
            it.authors shouldNotBe null
            it.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                ),
                Author(
                    id = resourceAuthor.id,
                    name = "Author 2",
                    identifiers = mapOf(
                        "orcid" to listOf("0000-1111-2222-3333")
                    ),
                    homepage = ParsedIRI("https://example.org")
                )
            )
            it.versions shouldBe VersionInfo(
                head = HeadVersion(unpublished),
                published = listOf(
                    PublishedVersion(expected, changelog.label),
                    PublishedVersion(publishedVersion1, changelog1.label)
                )
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_1"), "No poverty")
            )
            it.observatories shouldBe setOf(expected.observatoryId)
            it.organizations shouldBe setOf(expected.organizationId)
            it.extractionMethod shouldBe expected.extractionMethod
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe expected.unlistedBy
            it.published shouldBe true
            it.sections shouldBe listOf(
                SmartReviewTextSection(textSection.id, textSection.label, setOf(Classes.introduction), textContent),
                SmartReviewComparisonSection(comparisonSection.id, "comparison section heading", ResourceReference(comparison)),
                SmartReviewVisualizationSection(visualizationSection.id, "visualization section heading", ResourceReference(visualization)),
                SmartReviewResourceSection(resourceSection.id, "resource section heading", ResourceReference(resource)),
                SmartReviewPredicateSection(propertySection.id, "property section heading", PredicateReference(property)),
                SmartReviewOntologySection(
                    id = ontologySection.id,
                    heading = "ontology section heading",
                    entities = listOf(ResourceReference(ontologyResourceEntity), PredicateReference(ontologyPredicateEntity)),
                    predicates = listOf(PredicateReference(ontologyPredicate))
                )
            )
            it.references shouldBe listOf("reference 1", "reference 2")
            it.acknowledgements shouldBe mapOf(
                ContributorId.UNKNOWN to 0.875,
                ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c") to 0.125
            )
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = unpublished.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents (comparison), then it is returned`() {
        val smartReview = createResource(classes = setOf(Classes.smartReviewPublished))
        val content = createResource(id = ThingId("R123"), classes = setOf(Classes.comparison))
        val versions = VersionInfo(HeadVersion(content), emptyList())

        every { resourceRepository.findById(smartReview.id) } returns Optional.of(smartReview)
        every { smartReviewPublishedRepository.findById(smartReview.id) } returns Optional.of(
            PublishedContentType(
                id = smartReview.id,
                rootId = ThingId("R456"),
                subgraph = listOf(
                    createStatement(
                        subject = createResource(classes = setOf(Classes.comparisonSection)),
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = content
                    )
                )
            )
        )
        every { statementRepository.fetchAsBundle(any(), any(), any()) } returns emptyList()
        every { with(comparisonService) { content.findTableData() } } returns ComparisonTable.empty(content.id)
        every { with(comparisonService) { content.findVersionInfo(any()) } } returns versions

        val result = service.findPublishedContentById(smartReview.id, content.id)
        result.isLeft shouldBe true
        result.onLeft { comparison ->
            comparison.shouldBeInstanceOf<Comparison>().asClue {
                it.id shouldBe content.id
                it.title shouldBe content.label
                it.createdAt shouldBe content.createdAt
                it.createdBy shouldBe content.createdBy
                it.observatories shouldBe listOf(content.observatoryId)
                it.extractionMethod shouldBe content.extractionMethod
                it.organizations shouldBe listOf(content.organizationId)
                it.visibility shouldBe content.visibility
                it.unlistedBy shouldBe content.unlistedBy
                it.versions shouldBe versions
            }
        }

        verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(smartReview.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(any(), any(), any()) }
        verify(exactly = 1) { with(comparisonService) { content.findTableData() } }
        verify(exactly = 1) { with(comparisonService) { content.findVersionInfo(any()) } }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents (visualization), then it is returned`() {
        val smartReview = createResource(classes = setOf(Classes.smartReviewPublished))
        val content = createResource(id = ThingId("R123"), classes = setOf(Classes.visualization))

        every { resourceRepository.findById(smartReview.id) } returns Optional.of(smartReview)
        every { smartReviewPublishedRepository.findById(smartReview.id) } returns Optional.of(
            PublishedContentType(
                id = smartReview.id,
                rootId = ThingId("R456"),
                subgraph = listOf(
                    createStatement(
                        subject = createResource(classes = setOf(Classes.visualizationSection)),
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = content
                    )
                )
            )
        )
        every { statementRepository.fetchAsBundle(any(), any(), any()) } returns emptyList()

        val result = service.findPublishedContentById(smartReview.id, content.id)
        result.isLeft shouldBe true
        result.onLeft { visualization ->
            visualization.shouldBeInstanceOf<Visualization>().asClue {
                it.id shouldBe content.id
                it.title shouldBe content.label
                it.createdAt shouldBe content.createdAt
                it.createdBy shouldBe content.createdBy
                it.observatories shouldBe listOf(content.observatoryId)
                it.extractionMethod shouldBe content.extractionMethod
                it.organizations shouldBe listOf(content.organizationId)
                it.visibility shouldBe content.visibility
                it.unlistedBy shouldBe content.unlistedBy
            }
        }

        verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(smartReview.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(any(), any(), any()) }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents (subgraph), then it is returned`() {
        val smartReview = createResource(classes = setOf(Classes.smartReviewPublished))
        val content = createResource(id = ThingId("R123"), classes = setOf(Classes.model))

        every { resourceRepository.findById(smartReview.id) } returns Optional.of(smartReview)
        every { smartReviewPublishedRepository.findById(smartReview.id) } returns Optional.of(
            PublishedContentType(
                id = smartReview.id,
                rootId = ThingId("R456"),
                subgraph = listOf(
                    createStatement(
                        subject = createResource(classes = setOf(Classes.section)),
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = content
                    ),
                    createStatement(subject = content),
                    createStatement(subject = createResource(ThingId("unrelated")))
                )
            )
        )
        every { statementRepository.fetchAsBundle(any(), any(), any()) } returns emptyList()

        val result = service.findPublishedContentById(smartReview.id, content.id)
        result.isRight shouldBe true
        result.onRight { statements ->
            statements shouldBe listOf(createStatement(subject = content))
        }

        verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(smartReview.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(any(), any(), any()) }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents but smart review does not exist, then it throws an exception`() {
        val smartReviewId = ThingId("R123")
        val contentId = ThingId("R456")

        every { resourceRepository.findById(smartReviewId) } returns Optional.empty()

        assertThrows<SmartReviewNotFound> {
            service.findPublishedContentById(smartReviewId, contentId)
        }

        verify(exactly = 1) { resourceRepository.findById(smartReviewId) }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents but provided smart review id is not of a published smart review, then it throws an exception`() {
        val notASmartReview = createResource(classes = setOf(Classes.paper))
        val contentId = ThingId("R456")

        every { resourceRepository.findById(notASmartReview.id) } returns Optional.of(notASmartReview)

        assertThrows<SmartReviewNotFound> {
            service.findPublishedContentById(notASmartReview.id, contentId)
        }

        verify(exactly = 1) { resourceRepository.findById(notASmartReview.id) }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents but content is unrelated, then it throws an exception`() {
        val smartReview = createResource(classes = setOf(Classes.smartReviewPublished))
        val unrelatedContent = createResource(id = ThingId("R123"), classes = setOf(Classes.dataset))

        every { resourceRepository.findById(smartReview.id) } returns Optional.of(smartReview)
        every { smartReviewPublishedRepository.findById(smartReview.id) } returns Optional.of(
            PublishedContentType(
                id = smartReview.id,
                rootId = ThingId("R456"),
                subgraph = listOf()
            )
        )
        every { statementRepository.fetchAsBundle(any(), any(), any()) } returns emptyList()

        assertThrows<PublishedSmartReviewContentNotFound> {
            service.findPublishedContentById(smartReview.id, unrelatedContent.id)
        }

        verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(smartReview.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(any(), any(), any()) }
    }

    @Test
    fun `Given a published smart review, when fetching one of its contents but content is not of a know class, then it throws an exception`() {
        val smartReview = createResource(classes = setOf(Classes.smartReviewPublished))
        val unknownContent = createResource(id = ThingId("R123"), classes = setOf(Classes.caption))

        every { resourceRepository.findById(smartReview.id) } returns Optional.of(smartReview)
        every { smartReviewPublishedRepository.findById(smartReview.id) } returns Optional.of(
            PublishedContentType(
                id = smartReview.id,
                rootId = ThingId("R456"),
                subgraph = listOf()
            )
        )
        every { statementRepository.fetchAsBundle(any(), any(), any()) } returns emptyList()

        assertThrows<PublishedSmartReviewContentNotFound> {
            service.findPublishedContentById(smartReview.id, unknownContent.id)
        }

        verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
        verify(exactly = 1) { smartReviewPublishedRepository.findById(smartReview.id) }
        verify(exactly = 1) { statementRepository.fetchAsBundle(any(), any(), any()) }
    }
}
