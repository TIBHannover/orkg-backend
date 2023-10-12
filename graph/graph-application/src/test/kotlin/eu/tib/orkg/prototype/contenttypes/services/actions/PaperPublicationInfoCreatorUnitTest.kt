package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page

@Nested
class PaperPublicationInfoCreatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperPublicationInfoCreator = PaperPublicationInfoCreator(
        resourceService = resourceService,
        resourceRepository = resourceRepository,
        statementService = statementService,
        literalService = literalService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, resourceService, statementService, literalService)
    }

    @Test
    fun `Given a paper create command, when linking empty publication info, it returns success`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = null
        )
        val state = PaperState(
            paperId = paperId
        )

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }
    }

    @Test
    fun `Given a paper create command, when linking publication month, it returns success`() {
        val paperId = ThingId("R123")
        val month = 5
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = PaperState(
            paperId = paperId
        )
        val monthLiteral = createLiteral(label = month.toString())

        every {
            literalService.create(
                userId = command.contributorId,
                label = month.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns monthLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.monthPublished,
                `object` = monthLiteral.id
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = month.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.monthPublished,
                `object` = monthLiteral.id
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking publication year, it returns success`() {
        val paperId = ThingId("R123")
        val year = 5L
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = year,
                publishedIn = null,
                url = null
            )
        )
        val state = PaperState(
            paperId = paperId
        )
        val yearLiteral = createLiteral(label = year.toString())

        every {
            literalService.create(
                userId = command.contributorId,
                label = year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } returns yearLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.yearPublished,
                `object` = yearLiteral.id
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.yearPublished,
                `object` = yearLiteral.id
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking existing publication venue, it returns success`() {
        val paperId = ThingId("R123")
        val venue = "Conference"
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = PaperState(
            paperId = paperId
        )
        val venueResource = createResource(label = venue)

        every {
            resourceRepository.findAllByClassAndLabel(
                `class` = Classes.venue,
                labelSearchString = any(),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(venueResource)
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasVenue,
                `object` = venueResource.id
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            resourceRepository.findAllByClassAndLabel(
                Classes.venue,
                withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBeEqualIgnoringCase venue
                },
                PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasVenue,
                `object` = venueResource.id
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking non-existing publication venue, it returns success`() {
        val paperId = ThingId("R123")
        val venue = "Conference"
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = PaperState(
            paperId = paperId
        )
        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = venue,
            classes = setOf(Classes.venue),
            contributorId = command.contributorId
        )
        val venueId = ThingId("R456")

        every {
            resourceRepository.findAllByClassAndLabel(
                Classes.venue,
                any(),
                PageRequests.SINGLE
            )
        } returns Page.empty()
        every { resourceService.create(resourceCreateCommand) } returns venueId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasVenue,
                `object` = venueId
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            resourceRepository.findAllByClassAndLabel(
                Classes.venue,
                withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBeEqualIgnoringCase venue
                },
                PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { resourceService.create(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasVenue,
                `object` = venueId
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking publication url, it returns success`() {
        val paperId = ThingId("R123")
        val url = URI.create("https://orkg.org")
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = url
            )
        )
        val state = PaperState(
            paperId = paperId
        )
        val urlLiteral = createLiteral(label = url.toString())

        every {
            literalService.create(
                userId = command.contributorId,
                label = url.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        } returns urlLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasURL,
                `object` = urlLiteral.id
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = url.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasURL,
                `object` = urlLiteral.id
            )
        }
    }
}
