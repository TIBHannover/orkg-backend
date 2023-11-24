package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

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
        every {
            statementRepository.findAllBySubjectAndPredicate(
                authorList.id,
                Predicates.hasListElement,
                any()
            )
        } returns pageOf(
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
        verify(exactly = 1) {
            statementRepository.findAllBySubjectAndPredicate(
                authorList.id,
                Predicates.hasListElement,
                any()
            )
        }
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
        every {
            statementRepository.findAllBySubjectAndPredicate(
                authorList.id,
                Predicates.hasListElement,
                any()
            )
        } returns pageOf(
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
        verify(exactly = 1) {
            statementRepository.findAllBySubjectAndPredicate(
                authorList.id,
                Predicates.hasListElement,
                any()
            )
        }
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

    @Test
    fun `Given a comparison related resource create command, it creates the comparison related resource`() {
        val command = CreateComparisonUseCase.CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )
        val resourceId = ThingId("R456")
        val comparison = createResource(classes = setOf(Classes.comparison))
        val image = createLiteral(ThingId("L1"))
        val url = createLiteral(ThingId("L2"))
        val description = createLiteral(ThingId("L3"))

        every { resourceRepository.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                )
            )
        } returns resourceId
        every { literalService.create(command.contributorId, command.image!!) } returns image
        every { literalService.create(command.contributorId, command.url!!) } returns url
        every { literalService.create(command.contributorId, command.description!!) } returns description
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasImage,
                `object` = image.id
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasURL,
                `object` = url.id
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.description,
                `object` = description.id
            )
        } just runs

        service.createComparisonRelatedResource(command) shouldBe resourceId

        verify(exactly = 1) { resourceRepository.findById(command.comparisonId) }
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                )
            )
        }
        verify(exactly = 1) { literalService.create(command.contributorId, command.image!!) }
        verify(exactly = 1) { literalService.create(command.contributorId, command.url!!) }
        verify(exactly = 1) { literalService.create(command.contributorId, command.description!!) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasImage,
                `object` = image.id
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.hasURL,
                `object` = url.id
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = resourceId,
                predicate = Predicates.description,
                `object` = description.id
            )
        }
    }

    @Test
    fun `Given a comparison related resource create command, when comparison does not exist, it throws an exception`() {
        val command = CreateComparisonUseCase.CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = null,
            url = null,
            description = null
        )

        every { resourceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.createComparisonRelatedResource(command) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a comparison related figure create command, it creates the comparison related figure`() {
        val command = CreateComparisonUseCase.CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = "https://example.org/test.png",
            description = "comparison related figure description"
        )
        val figureId = ThingId("R456")
        val comparison = createResource(classes = setOf(Classes.comparison))
        val image = createLiteral(ThingId("L1"))
        val description = createLiteral(ThingId("L3"))

        every { resourceRepository.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedFigure),
                )
            )
        } returns figureId
        every { literalService.create(command.contributorId, command.image!!) } returns image
        every { literalService.create(command.contributorId, command.description!!) } returns description
        every {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.hasImage,
                `object` = image.id
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.description,
                `object` = description.id
            )
        } just runs

        service.createComparisonRelatedFigure(command) shouldBe figureId

        verify(exactly = 1) { resourceRepository.findById(command.comparisonId) }
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedFigure),
                )
            )
        }
        verify(exactly = 1) { literalService.create(command.contributorId, command.image!!) }
        verify(exactly = 1) { literalService.create(command.contributorId, command.description!!) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.hasImage,
                `object` = image.id
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = figureId,
                predicate = Predicates.description,
                `object` = description.id
            )
        }
    }

    @Test
    fun `Given a comparison related figure create command, when comparison does not exist, it throws an exception`() {
        val command = CreateComparisonUseCase.CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = null,
            description = null
        )

        every { resourceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.createComparisonRelatedFigure(command) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }
}
