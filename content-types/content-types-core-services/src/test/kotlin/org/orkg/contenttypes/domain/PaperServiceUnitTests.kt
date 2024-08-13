package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort

class PaperServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classService: ClassUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val publishingService: PublishingService = mockk()
    private val paperRepository: PaperRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val listRepository: ListRepository = mockk()
    private val paperPublishedRepository: PaperPublishedRepository = mockk()

    private val service = PaperService(
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        thingRepository = thingRepository,
        classService = classService,
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        predicateService = predicateService,
        listService = listService,
        listRepository = listRepository,
        publishingService = publishingService,
        paperRepository = paperRepository,
        classRepository = classRepository,
        paperPublishedRepository = paperPublishedRepository,
        paperPublishBaseUri = "https://orkg.org/paper/"
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            resourceRepository,
            statementRepository,
            observatoryRepository,
            organizationRepository,
            thingRepository,
            classService,
            resourceService,
            statementService,
            literalService,
            predicateService,
            listService,
            listRepository,
            publishingService,
            paperRepository,
            classRepository,
            paperPublishedRepository
        )
    }

    @Test
    fun `Given a paper exists, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.comparison),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val researchFieldId = ThingId("R20")
        val resourceAuthorId = ThingId("R132564")
        val doi = "10.1000/182"
        val publishedYear: Long = 2016
        val publishedMonth = 1
        val publishedInId = ThingId("R4153")
        val publishedIn = "Conference"
        val publishedUrl = "https://example.org/conference"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val resourceAuthor = createResource(id = resourceAuthorId, label = "Author 2", classes = setOf(Classes.author))
        val firstBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
            whitelist = emptyList()
        )
        val secondBundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 1,
            blacklist = emptyList(),
            whitelist = listOf(Classes.researchField, Classes.contribution, Classes.venue)
        )

        every { resourceRepository.findPaperById(expected.id) } returns Optional.of(expected)
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
                `object` = createLiteral(label = publishedYear.toString(), datatype = Literals.XSD.DECIMAL.prefixedUri)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.monthPublished),
                `object` = createLiteral(label = publishedMonth.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasURL),
                `object` = createLiteral(label = publishedUrl, datatype = Literals.XSD.URI.prefixedUri)
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
                predicate = createPredicate(Predicates.mentions),
                `object` = createResource(
                    classes = setOf(Classes.paper),
                    label = "Some paper",
                    id = ThingId("R159")
                )
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
                predicate = createPredicate(Predicates.hasResearchField),
                `object` = createResource(
                    id = researchFieldId,
                    classes = setOf(Classes.researchField),
                    label = "Research Field 1"
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution123")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasVenue),
                `object` = createResource(publishedInId, label = publishedIn)
            )
        )

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue { paper ->
            paper.id shouldBe expected.id
            paper.title shouldBe expected.label
            paper.researchFields shouldNotBe null
            paper.researchFields shouldBe listOf(
                ObjectIdAndLabel(id = researchFieldId, label = "Research Field 1")
            )
            paper.identifiers shouldNotBe null
            paper.identifiers shouldBe mapOf(
                "doi" to listOf(doi)
            )
            paper.publicationInfo shouldNotBe null
            paper.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe publishedMonth
                publicationInfo.publishedYear shouldBe publishedYear
                publicationInfo.publishedIn shouldBe ObjectIdAndLabel(publishedInId, publishedIn)
                publicationInfo.url shouldBe ParsedIRI(publishedUrl)
            }
            paper.authors shouldNotBe null
            paper.authors shouldBe listOf(
                Author(
                    id = null,
                    name = "Author 1",
                    identifiers = emptyMap(),
                    homepage = null
                ),
                Author(
                    id = resourceAuthorId,
                    name = "Author 2",
                    identifiers = mapOf(
                        "orcid" to listOf("0000-1111-2222-3333")
                    ),
                    homepage = ParsedIRI("https://example.org")
                )
            )
            paper.contributions shouldNotBe null
            paper.contributions shouldBe listOf(
                ObjectIdAndLabel(ThingId("Contribution123"), "Contribution")
            )
            paper.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_1"), "No poverty")
            )
            paper.mentionings shouldBe setOf(
                ResourceReference(
                    id = ThingId("R159"),
                    label = "Some paper",
                    classes = setOf(Classes.paper)
                )
            )
            paper.observatories shouldBe setOf(expected.observatoryId)
            paper.organizations shouldBe setOf(expected.organizationId)
            paper.extractionMethod shouldBe expected.extractionMethod
            paper.createdAt shouldBe expected.createdAt
            paper.createdBy shouldBe expected.createdBy
            paper.verified shouldBe false
            paper.visibility shouldBe Visibility.DEFAULT
            paper.unlistedBy shouldBe expected.unlistedBy
        }

        verify(exactly = 1) { resourceRepository.findPaperById(expected.id) }
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
    }

    @Test
    fun `Given a paper, when fetching its contributors, then the list of contributors returned`() {
        val expected = createResource()
        every { resourceRepository.findPaperById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllContributorsByResourceId(expected.id, PageRequests.ALL) } returns pageOf(expected.createdBy)

        service.findAllContributorsByPaperId(expected.id, PageRequests.ALL)

        verify(exactly = 1) { resourceRepository.findPaperById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllContributorsByResourceId(expected.id, any()) }
    }

    @Test
    fun `Given a paper does not exist, when fetching its contributors, then an exception is thrown`() {
        val id = ThingId("Missing")
        every { resourceRepository.findPaperById(id) } returns Optional.empty()

        assertThrows<PaperNotFound> {
            service.findAllContributorsByPaperId(id, PageRequests.ALL)
        }

        verify(exactly = 1) { resourceRepository.findPaperById(id) }
        verify(exactly = 0) { statementRepository.findAllContributorsByResourceId(id, any()) }
    }

    @Test
    fun `Given a paper, when publishing, it returns success`() {
        val paper = createResource()
        val subject = "Paper subject"
        val description = "Fancy paper description"
        val contributorId = ContributorId(UUID.randomUUID())
        val authors = listOf(
            Author(
                id = null,
                name = "Author 1",
                identifiers = emptyMap(),
                homepage = null
            ),
            Author(
                id = ThingId("R132564"),
                name = "Author 2",
                identifiers = mapOf(
                    "orcid" to listOf("0000-1111-2222-3333")
                ),
                homepage = ParsedIRI("https://example.org")
            )
        )
        val paperVersionId = ThingId("R156416")

        every { resourceRepository.findPaperById(paper.id) } returns Optional.of(paper)
        every { publishingService.publish(any()) } returns paperVersionId

        val result = service.publish(
            PublishPaperUseCase.PublishCommand(
                id = paper.id,
                contributorId = contributorId,
                subject = subject,
                description = description,
                authors = authors
            )
        )
        result shouldBe paperVersionId

        verify(exactly = 1) { resourceRepository.findPaperById(paper.id) }
        verify(exactly = 1) {
            publishingService.publish(
                withArg {
                    it.id shouldBe paper.id
                    it.title shouldBe paper.label
                    it.contributorId shouldBe contributorId
                    it.subject shouldBe subject
                    it.description shouldBe description
                    it.url shouldBe URI.create("https://orkg.org/paper/${paper.id}")
                    it.creators shouldBe authors
                    it.resourceType shouldBe Classes.paper
                    it.relatedIdentifiers shouldBe emptyList()
                    it.snapshotCreator shouldNotBe null
                }
            )
        }
    }

    @Test
    fun `Given a paper, when publishing but service reports missing paper, it throws an exception`() {
        val id = ThingId("Missing")
        val contributorId = ContributorId(UUID.randomUUID())

        every { resourceRepository.findPaperById(id) } returns Optional.empty()

        shouldThrow<PaperNotFound> {
            service.publish(
                PublishPaperUseCase.PublishCommand(
                    id = id,
                    contributorId = contributorId,
                    subject = "Paper subject",
                    description = "Fancy paper description",
                    authors = emptyList()
                )
            )
        }

        verify(exactly = 1) { resourceRepository.findPaperById(id) }
    }
}
