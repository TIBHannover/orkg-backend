package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.statements.testing.fixtures.createStatement
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaperServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: PostgresOrganizationRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val publishingService: PublishingService = mockk()

    private val service = PaperService(
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        thingRepository = thingRepository,
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        predicateService = predicateService,
        listService = listService,
        publishingService = publishingService,
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
            resourceService,
            statementService,
            literalService,
            predicateService,
            listService,
            publishingService
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
        val publishedIn = "Conference"
        val publishedUrl = "https://example.org/conference"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))

        every { resourceRepository.findPaperById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, any()) } returns pageOf(
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
                predicate = createPredicate(Predicates.hasVenue),
                `object` = createLiteral(label = publishedIn)
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
                subject = expected,
                predicate = createPredicate(Predicates.hasContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution123")
                )
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createResource(id = resourceAuthorId, label = "Author 2", classes = setOf(Classes.author))
            )
        )
        every { statementRepository.findAllBySubject(resourceAuthorId, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasWebsite),
                `object` = createLiteral(label = "https://example.org", datatype = Literals.XSD.URI.prefixedUri)
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
                "doi" to doi
            )
            paper.publicationInfo shouldNotBe null
            paper.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe publishedMonth
                publicationInfo.publishedYear shouldBe publishedYear
                publicationInfo.publishedIn shouldBe publishedIn
                publicationInfo.url shouldBe URI.create(publishedUrl)
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
                        "orcid" to "0000-1111-2222-3333"
                    ),
                    homepage = URI.create("https://example.org")
                )
            )
            paper.contributions shouldNotBe null
            paper.contributions shouldBe listOf(
                ObjectIdAndLabel(ThingId("Contribution123"), "Contribution")
            )
            paper.observatories shouldBe setOf(expected.observatoryId)
            paper.organizations shouldBe setOf(expected.organizationId)
            paper.extractionMethod shouldBe expected.extractionMethod
            paper.createdAt shouldBe expected.createdAt
            paper.createdBy shouldBe expected.createdBy
            paper.verified shouldBe false
            paper.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findPaperById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubject(resourceAuthorId, any()) }
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
        val resourceAuthorId = ThingId("R132564")
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))

        every { resourceRepository.findPaperById(paper.id) } returns Optional.of(paper)
        every { statementRepository.findAllBySubject(paper.id, PageRequests.ALL) } returns pageOf(
            createStatement(
                subject = paper,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = paper,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            ),
            createStatement(
                subject = paper,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createResource(id = resourceAuthorId, label = "Author 2", classes = setOf(Classes.author))
            )
        )
        every { statementRepository.findAllBySubject(resourceAuthorId, any()) } returns pageOf(
            createStatement(
                subject = paper,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            ),
            createStatement(
                subject = paper,
                predicate = createPredicate(Predicates.hasWebsite),
                `object` = createLiteral(label = "https://example.org", datatype = Literals.XSD.URI.prefixedUri)
            )
        )
        every { publishingService.publish(any()) } returns "1324/56789"

        service.publish(paper.id, subject, description)

        verify(exactly = 1) { resourceRepository.findPaperById(paper.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(paper.id, PageRequests.ALL) }
        verify(exactly = 1) {
            publishingService.publish(
                withArg {
                    it.id shouldBe paper.id
                    it.title shouldBe paper.label
                    it.subject shouldBe subject
                    it.description shouldBe description
                    it.url shouldBe URI.create("https://orkg.org/paper/${paper.id}")
                    it.creators shouldBe listOf(
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
                                "orcid" to "0000-1111-2222-3333"
                            ),
                            homepage = URI.create("https://example.org")
                        )
                    )
                    it.resourceType shouldBe Classes.paper
                    it.relatedIdentifiers shouldBe emptyList()
                }
            )
        }
        verify(exactly = 1) { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubject(resourceAuthorId, any()) }
    }

    @Test
    fun `Given a paper, when publishing but service reports missing paper, it throws an exception`() {
        val id = ThingId("Missing")

        every { resourceRepository.findPaperById(id) } returns Optional.empty()

        shouldThrow<PaperNotFound> {
            service.publish(id, "Paper subject", "Fancy paper description")
        }

        verify(exactly = 1) { resourceRepository.findPaperById(id) }
    }
}
