package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.application.ComparisonNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.identifiers.domain.DOI
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.statements.testing.fixtures.createStatement
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.Test

class ComparisonServiceUnitTests {
    private val contributionComparisonRepository: ContributionComparisonRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: PostgresOrganizationRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val researchFieldService: RetrieveResearchFieldUseCase = mockk()
    private val publishingService: PublishingService = mockk()

    private val service = ComparisonService(
        repository = contributionComparisonRepository,
        resourceRepository = resourceRepository,
        statementRepository = statementRepository,
        observatoryRepository = observatoryRepository,
        organizationRepository = organizationRepository,
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        listService = listService,
        researchFieldService = researchFieldService,
        publishingService = publishingService,
        comparisonPublishBaseUri = "https://orkg.org/comparison/"
    )

    @Test
    fun `Given a comparison exists, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.comparison),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val previousVersion = ThingId("R156")
        val researchFieldId = ThingId("R20")
        val doi = "10.1000/182"
        val publicationYear: Long = 2016
        val publicationMonth = 1
        val reference = "https://orkg.org"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, any()) } returns pageOf(
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
                predicate = createPredicate(Predicates.hasDOI),
                `object` = createLiteral(label = doi)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.yearPublished),
                `object` = createLiteral(label = publicationYear.toString(), datatype = Literals.XSD.DECIMAL.prefixedUri)
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
                predicate = createPredicate(Predicates.comparesContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasVisualization),
                `object` = createResource(
                    classes = setOf(Classes.visualization),
                    label = "Visualization",
                    id = ThingId("Visualization")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedResource),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedResource),
                    label = "ComparisonRelatedResource",
                    id = ThingId("ComparisonRelatedResource")
                )
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasRelatedFigure),
                `object` = createResource(
                    classes = setOf(Classes.comparisonRelatedFigure),
                    label = "ComparisonRelatedFigure",
                    id = ThingId("ComparisonRelatedFigure")
                )
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
                subject = expected,
                predicate = createPredicate(Predicates.hasPreviousVersion),
                `object` = createResource(id = previousVersion)
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            )
        )

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
                "doi" to doi
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
            comparison.contributions shouldNotBe null
            comparison.contributions shouldBe listOf(
                ObjectIdAndLabel(ThingId("Contribution"), "Contribution")
            )
            comparison.visualizations shouldNotBe null
            comparison.visualizations shouldBe listOf(
                ObjectIdAndLabel(ThingId("Visualization"), "Visualization")
            )
            comparison.relatedFigures shouldNotBe null
            comparison.relatedFigures shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedFigure"), "ComparisonRelatedFigure")
            )
            comparison.relatedResources shouldNotBe null
            comparison.relatedResources shouldBe listOf(
                ObjectIdAndLabel(ThingId("ComparisonRelatedResource"), "ComparisonRelatedResource")
            )
            comparison.references shouldNotBe null
            comparison.references shouldBe listOf(reference)
            comparison.observatories shouldBe setOf(expected.observatoryId)
            comparison.organizations shouldBe setOf(expected.organizationId)
            comparison.extractionMethod shouldBe expected.extractionMethod
            comparison.createdAt shouldBe expected.createdAt
            comparison.createdBy shouldBe expected.createdBy
            comparison.previousVersion shouldBe previousVersion
            comparison.isAnonymized shouldBe true
            comparison.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) }
    }

    @Test
    fun `Given a comparison, when publishing, it returns success`() {
        val comparison = createResource(
            classes = setOf(Classes.comparison),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val resourceAuthorId = ThingId("R132564")
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val relatedDoi = "10.1472/58369"

        every { resourceRepository.findById(comparison.id) } returns Optional.of(comparison)
        every { statementRepository.findAllBySubject(comparison.id, any()) } returns pageOf(
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = authorList
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.comparesContribution),
                `object` = createResource(
                    classes = setOf(Classes.contribution),
                    label = "Contribution",
                    id = ThingId("Contribution")
                )
            )
        )
        every { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) } returns pageOf(
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createLiteral(label = "Author 1")
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createResource(id = resourceAuthorId, label = "Author 2", classes = setOf(Classes.author))
            )
        )
        every { statementRepository.findAllBySubject(resourceAuthorId, any()) } returns pageOf(
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(Predicates.hasWebsite),
                `object` = createLiteral(label = "https://example.org", datatype = Literals.XSD.URI.prefixedUri)
            )
        )
        every { statementRepository.findAllDOIsRelatedToComparison(comparison.id) } returns listOf(relatedDoi)
        every { publishingService.publish(any()) } returns DOI.of("10.1234/56789")

        service.publish(comparison.id, "Research Field 1", "comparison description")

        verify(exactly = 1) { resourceRepository.findById(comparison.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(comparison.id, PageRequests.ALL) }
        verify(exactly = 1) { statementRepository.findAllBySubjectAndPredicate(authorList.id, Predicates.hasListElement, any()) }
        verify(exactly = 1) { statementRepository.findAllBySubject(resourceAuthorId, any()) }
        verify(exactly = 1) { statementRepository.findAllDOIsRelatedToComparison(comparison.id) }
        verify(exactly = 1) {
            publishingService.publish(
                withArg {
                    it.id shouldBe comparison.id
                    it.title shouldBe comparison.label
                    it.subject shouldBe "Research Field 1"
                    it.description shouldBe "comparison description"
                    it.url shouldBe URI.create("https://orkg.org/comparison/${comparison.id}")
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
                    it.resourceType shouldBe Classes.comparison
                    it.relatedIdentifiers shouldBe listOf(relatedDoi)
                }
            )
        }
    }

    @Test
    fun `Given a comparison, when publishing but service reports missing paper, it throws an exception`() {
        val id = ThingId("Missing")

        every { resourceRepository.findById(id) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.publish(id, "Comparison subject", "Fancy comparison description") }

        verify(exactly = 1) { resourceRepository.findById(id) }
    }
}
