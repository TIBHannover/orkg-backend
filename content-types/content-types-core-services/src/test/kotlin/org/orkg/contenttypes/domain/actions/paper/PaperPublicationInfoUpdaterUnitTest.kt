package org.orkg.contenttypes.domain.actions.paper

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
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

class PaperPublicationInfoUpdaterUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperPublicationInfoUpdater = PaperPublicationInfoUpdater(
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
    fun `Given a paper update command, when updating with empty publication info, it does nothing`() {
        val paper = createDummyPaper()
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = null
        )
        val state = UpdatePaperState(
            paper = paper
        )

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication month with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = 5,
            publishedYear = null,
            publishedIn = null,
            url = null
        )
        val paper = createDummyPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = publicationInfo
        )
        val state = UpdatePaperState(
            paper = paper
        )

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication month with null, it deletes the old literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = 5,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val monthLiteral = createLiteral(label = paper.publicationInfo.publishedMonth.toString())
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.monthPublished,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.monthPublished),
                `object` = monthLiteral
            )
        )
        every { statementService.delete(statementId) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.monthPublished,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a paper update command, when updating publication month with a new value, it creates a new literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val month = 5
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val monthLiteralId = ThingId("L132")

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns monthLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.monthPublished,
                `object` = monthLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.monthPublished,
                `object` = monthLiteralId
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication month with a new value, it replaces the old statement`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = 5,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = 6,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val oldMonthLiteral = createLiteral(
            label = paper.publicationInfo.publishedMonth.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val newMonthLiteralId = ThingId("L534")
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.monthPublished,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.monthPublished),
                `object` = oldMonthLiteral
            )
        )
        every { statementService.delete(statementId) } just runs
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedMonth.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns newMonthLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.monthPublished,
                `object` = newMonthLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.monthPublished,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedMonth.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.monthPublished,
                `object` = newMonthLiteralId
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication year with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = 2023,
            publishedIn = null,
            url = null
        )
        val paper = createDummyPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = publicationInfo
        )
        val state = UpdatePaperState(
            paper = paper
        )

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication year with null, it deletes the old literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = 2023,
                publishedIn = null,
                url = null
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val yearLiteral = createLiteral(label = paper.publicationInfo.publishedYear.toString())
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.yearPublished,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.yearPublished),
                `object` = yearLiteral
            )
        )
        every { statementService.delete(statementId) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.yearPublished,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a paper update command, when updating publication year with a new value, it creates a new literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val year: Long = 2023
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = year,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val yearLiteralId = ThingId("L645")

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns yearLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.yearPublished,
                `object` = yearLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.yearPublished,
                `object` = yearLiteralId
            )
        }
    }
    
    @Test
    fun `Given a paper update command, when updating publication year with a new value, it replaces the old statement`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = 2022,
                publishedIn = null,
                url = null
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = 2023,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val oldYearLiteral = createLiteral(
            label = paper.publicationInfo.publishedYear.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val newYearLiteralId = ThingId("L4351")
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.yearPublished,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.yearPublished),
                `object` = oldYearLiteral
            )
        )
        every { statementService.delete(statementId) } just runs
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns newYearLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.yearPublished,
                `object` = newYearLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.yearPublished,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.yearPublished,
                `object` = newYearLiteralId
            )
        }
    }
    
    @Test
    fun `Given a paper update command, when updating publication venue with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = null,
            publishedIn = "Conference",
            url = null
        )
        val paper = createDummyPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = publicationInfo
        )
        val state = UpdatePaperState(
            paper = paper
        )

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with null, it deletes the old statement`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = "Conference",
                url = null
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val venueResource = createResource(
            label = paper.publicationInfo.publishedIn.toString(),
            classes = setOf(Classes.venue)
        )
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasVenue,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasVenue),
                `object` = venueResource
            )
        )
        every { statementService.delete(statementId) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasVenue,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with a new value, it creates a new venue resource`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val venue = "Conference"
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
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
                subject = command.paperId,
                predicate = Predicates.hasVenue,
                `object` = venueId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
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
                subject = command.paperId,
                predicate = Predicates.hasVenue,
                `object` = venueId
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with a new value, it reuses an existing venue resource`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val venue = "Conference"
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val venueResource = createResource(
            label = venue,
            classes = setOf(Classes.venue)
        )

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
                subject = command.paperId,
                predicate = Predicates.hasVenue,
                `object` = venueResource.id
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.hasVenue,
                `object` = venueResource.id
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
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
                subject = command.paperId,
                predicate = Predicates.hasVenue,
                `object` = venueResource.id
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = null,
            publishedIn = null,
            url = URI.create("https://orkg.org/")
        )
        val paper = createDummyPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = publicationInfo
        )
        val state = UpdatePaperState(
            paper = paper
        )

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with null, it deletes the old literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = URI.create("https://orkg.org/")
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val urlLiteral = createLiteral(label = paper.publicationInfo.url.toString())
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasURL,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasURL),
                `object` = urlLiteral
            )
        )
        every { statementService.delete(statementId) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasURL,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
    }

    @Test
    fun `Given a paper update command, when updating publication url with a new value, it creates a new literal`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val url = URI.create("https://orkg.org/")
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = url
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val urlLiteralId = ThingId("L4356")

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns urlLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.hasURL,
                `object` = urlLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.hasURL,
                `object` = urlLiteralId
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with a new value, it replaces the old statement`() {
        val paper = createDummyPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = URI.create("https://example.org")
            )
        )
        val command = dummyUpdatePaperCommand().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = URI.create("https://orkg.org")
            )
        )
        val state = UpdatePaperState(
            paper = paper
        )
        val oldUrlLiteral = createLiteral(
            label = paper.publicationInfo.url.toString(),
            datatype = Literals.XSD.URI.prefixedUri
        )
        val newUrlLiteralId = ThingId("L15436")
        val statementId = StatementId("S1")

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasURL,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasURL),
                `object` = oldUrlLiteral
            )
        )
        every { statementService.delete(statementId) } just runs
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns newUrlLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.hasURL,
                `object` = newUrlLiteralId
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = command.paperId,
                predicateId = Predicates.hasURL,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementService.delete(statementId) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.paperId,
                predicate = Predicates.hasURL,
                `object` = newUrlLiteralId
            )
        }
    }
}
