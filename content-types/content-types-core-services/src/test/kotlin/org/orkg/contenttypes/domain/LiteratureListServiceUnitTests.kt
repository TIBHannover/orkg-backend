package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.springframework.data.domain.Sort

class LiteratureListServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val literatureListRepository: LiteratureListRepository = mockk()
    private val literatureListPublishedRepository: LiteratureListPublishedRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val listRepository: ListRepository = mockk()

    private val service = LiteratureListService(
        resourceRepository,
        literatureListRepository,
        literatureListPublishedRepository,
        statementRepository,
        observatoryRepository,
        organizationRepository,
        resourceService,
        literalService,
        statementService,
        listService,
        listRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            resourceRepository,
            literatureListRepository,
            literatureListPublishedRepository,
            statementRepository,
            observatoryRepository,
            organizationRepository,
            resourceService,
            literalService,
            statementService,
            listService,
            listRepository
        )
    }

    @Test
    fun `Given an unpublished literature list, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.literatureList),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val researchFieldId = ThingId("R20")
        val publishedVersion1 = createResource(
            id = ThingId("R235467"),
            label = "published1",
            classes = setOf(Classes.literatureListPublished)
        )
        val changelog1 = createLiteral(id = ThingId("L13546"), label = "changelog1")
        val publishedVersion2 = createResource(
            id = ThingId("R154687"),
            label = "published2",
            classes = setOf(Classes.literatureListPublished)
        )
        val changelog2 = createLiteral(id = ThingId("L5753"), label = "changelog2")
        val listSection = createResource(id = ThingId("R489"), classes = setOf(Classes.listSection))
        val entry1 = createResource(id = ThingId("R646568"))
        val entry2 = createResource(id = ThingId("R645454"))
        val paper = createResource(id = ThingId("R47876"), classes = setOf(Classes.paper))
        val paperEntryDescription = "paper entry description"
        val comparison = createResource(id = ThingId("R1546878"), classes = setOf(Classes.comparison))
        val textSection = createResource(id = ThingId("R489"), label = "heading", classes = setOf(Classes.textSection))
        val textContent = "text content"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val resourceAuthor = createResource(id = ThingId("R132564"), label = "Author 2", classes = setOf(Classes.author))
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 3,
            blacklist = listOf(Classes.researchField, Classes.contribution, Classes.venue),
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
                predicate = createPredicate(Predicates.hasSection),
                `object` = textSection
            ),
            createStatement(
                subject = textSection,
                predicate = createPredicate(Predicates.hasHeadingLevel),
                `object` = createLiteral(label = 4.toString(), datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = textSection,
                predicate = createPredicate(Predicates.hasContent),
                `object` = createLiteral(label = textContent)
            ),
            createStatement(
                subject = expected,
                predicate = createPredicate(Predicates.hasSection),
                `object` = listSection
            ),
            createStatement(
                subject = listSection,
                predicate = createPredicate(Predicates.hasEntry),
                `object` = entry1
            ),
            createStatement(
                subject = entry1,
                predicate = createPredicate(Predicates.hasPaper),
                `object` = paper
            ),
            createStatement(
                subject = entry1,
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = paperEntryDescription)
            ),
            createStatement(
                subject = listSection,
                predicate = createPredicate(Predicates.hasEntry),
                `object` = entry2
            ),
            createStatement(
                subject = entry2,
                predicate = createPredicate(Predicates.hasLink),
                `object` = comparison
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
                    homepage = URI.create("https://example.org")
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
                TextSection(textSection.id, textSection.label, 4, textContent),
                ListSection(
                    listSection.id, listOf(
                        ListSection.Entry(ResourceReference(paper), paperEntryDescription),
                        ListSection.Entry(ResourceReference(comparison))
                    )
                )
            )
            it.acknowledgements shouldBe mapOf(
                ContributorId.UNKNOWN to 1.0
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
    fun `Given a published literature list, when fetching it by id, then it is returned`() {
        val unpublished = createResource(
            classes = setOf(Classes.literatureList),
        )
        val publishedVersion1 = createResource(
            id = ThingId("R235467"),
            label = "published1",
            classes = setOf(Classes.literatureListPublished),
            organizationId = OrganizationId(UUID.randomUUID()),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val changelog1 = createLiteral(id = ThingId("L13546"), label = "changelog1")

        val expected = createResource(
            id = ThingId("R154687"),
            label = "published2",
            classes = setOf(Classes.literatureListPublished)
        )
        val changelog = createLiteral(id = ThingId("L5753"), label = "changelog2")

        val researchFieldId = ThingId("R20")
        val listSection = createResource(id = ThingId("R489"), classes = setOf(Classes.listSection))
        val entry1 = createResource(id = ThingId("R646568"))
        val entry2 = createResource(id = ThingId("R645454"))
        val paper = createResource(id = ThingId("R47876"), classes = setOf(Classes.paper))
        val paperEntryDescription = "paper entry description"
        val comparison = createResource(id = ThingId("R1546878"), classes = setOf(Classes.comparison))
        val textSection = createResource(id = ThingId("R489"), label = "heading", classes = setOf(Classes.textSection))
        val textContent = "text content"
        val authorList = createResource(classes = setOf(Classes.list), id = ThingId("R536456"))
        val resourceAuthor = createResource(id = ThingId("R132564"), label = "Author 2", classes = setOf(Classes.author))
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 2,
            blacklist = emptyList(),
            whitelist = listOf(Classes.literatureList, Classes.literatureListPublished, Classes.literal)
        )

        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { literatureListPublishedRepository.findById(expected.id) } returns Optional.of(
            PublishedContentType(
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
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = textSection
                    ),
                    createStatement(
                        subject = textSection,
                        predicate = createPredicate(Predicates.hasHeadingLevel),
                        `object` = createLiteral(label = 4.toString(), datatype = Literals.XSD.INT.prefixedUri)
                    ),
                    createStatement(
                        subject = textSection,
                        predicate = createPredicate(Predicates.hasContent),
                        `object` = createLiteral(label = textContent)
                    ),
                    createStatement(
                        subject = unpublished,
                        predicate = createPredicate(Predicates.hasSection),
                        `object` = listSection
                    ),
                    createStatement(
                        subject = listSection,
                        predicate = createPredicate(Predicates.hasEntry),
                        `object` = entry1
                    ),
                    createStatement(
                        subject = entry1,
                        predicate = createPredicate(Predicates.hasPaper),
                        `object` = paper
                    ),
                    createStatement(
                        subject = entry1,
                        predicate = createPredicate(Predicates.description),
                        `object` = createLiteral(label = paperEntryDescription)
                    ),
                    createStatement(
                        subject = listSection,
                        predicate = createPredicate(Predicates.hasEntry),
                        `object` = entry2
                    ),
                    createStatement(
                        subject = entry2,
                        predicate = createPredicate(Predicates.hasLink),
                        `object` = comparison
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
                    homepage = URI.create("https://example.org")
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
                TextSection(textSection.id, textSection.label, 4, textContent),
                ListSection(
                    listSection.id,
                    listOf(
                        ListSection.Entry(ResourceReference(paper), paperEntryDescription),
                        ListSection.Entry(ResourceReference(comparison))
                    )
                )
            )
            it.acknowledgements shouldBe mapOf(
                ContributorId.UNKNOWN to 1.0
            )
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { literatureListPublishedRepository.findById(expected.id) }
        verify(exactly = 1) {
            statementRepository.fetchAsBundle(
                id = unpublished.id,
                configuration = bundleConfiguration,
                sort = Sort.unsorted()
            )
        }
    }
}
