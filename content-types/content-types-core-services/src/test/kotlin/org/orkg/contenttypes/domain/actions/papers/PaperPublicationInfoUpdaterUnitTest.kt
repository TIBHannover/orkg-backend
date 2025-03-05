package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

internal class PaperPublicationInfoUpdaterUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val paperPublicationInfoUpdater = PaperPublicationInfoUpdater(
        unsafeResourceUseCases = unsafeResourceUseCases,
        resourceRepository = resourceRepository,
        statementService = statementService,
        unsafeStatementUseCases = unsafeStatementUseCases,
        unsafeLiteralUseCases = unsafeLiteralUseCases
    )

    @Test
    fun `Given a paper update command, when updating with empty publication info, it does nothing`() {
        val paper = createPaper()
        val command = updatePaperCommand().copy(
            publicationInfo = null
        )
        val state = UpdatePaperState(paper)

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
        val paper = createPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = updatePaperCommand().copy(
            publicationInfo = publicationInfo.toPublicationInfoDefinition()
        )
        val state = UpdatePaperState(paper)

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication month with null, it deletes the old literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = 5,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val monthLiteral = createLiteral(label = paper.publicationInfo.publishedMonth.toString())
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.monthPublished),
                `object` = monthLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
    }

    @Test
    fun `Given a paper update command, when updating publication month with a new value, it creates a new literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val month = 5
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(paper)
        val monthLiteralId = ThingId("L132")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns monthLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.monthPublished,
                    objectId = monthLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.monthPublished,
                    objectId = monthLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication month with a new value, it replaces the old statement`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = 5,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = 6,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val oldMonthLiteral = createLiteral(
            label = paper.publicationInfo.publishedMonth.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val newMonthLiteralId = ThingId("L534")
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.monthPublished),
                `object` = oldMonthLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedMonth.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns newMonthLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.monthPublished,
                    objectId = newMonthLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedMonth.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.monthPublished,
                    objectId = newMonthLiteralId
                )
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
        val paper = createPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = updatePaperCommand().copy(
            publicationInfo = publicationInfo.toPublicationInfoDefinition()
        )
        val state = UpdatePaperState(paper)

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication year with null, it deletes the old literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = 2023,
                publishedIn = null,
                url = null
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val yearLiteral = createLiteral(label = paper.publicationInfo.publishedYear.toString())
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.yearPublished),
                `object` = yearLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
    }

    @Test
    fun `Given a paper update command, when updating publication year with a new value, it creates a new literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val year: Long = 2023
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = year,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(paper)
        val yearLiteralId = ThingId("L645")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns yearLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.yearPublished,
                    objectId = yearLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.yearPublished,
                    objectId = yearLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication year with a new value, it replaces the old statement`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = 2022,
                publishedIn = null,
                url = null
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = 2023,
                publishedIn = null,
                url = null
            )
        )
        val oldYearLiteral = createLiteral(
            label = paper.publicationInfo.publishedYear.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val newYearLiteralId = ThingId("L4351")
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.yearPublished),
                `object` = oldYearLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns newYearLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.yearPublished,
                    objectId = newYearLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.publishedYear.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.yearPublished,
                    objectId = newYearLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = null,
            publishedIn = ObjectIdAndLabel(ThingId("irrelevant"), "Conference"),
            url = null
        )
        val paper = createPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = updatePaperCommand().copy(
            publicationInfo = publicationInfo.toPublicationInfoDefinition()
        )
        val state = UpdatePaperState(paper)

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with null, it deletes the old statement`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = ObjectIdAndLabel(ThingId("irrelevant"), "Conference"),
                url = null
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val venueResource = createResource(
            label = paper.publicationInfo.publishedIn.toString(),
            classes = setOf(Classes.venue)
        )
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasVenue),
                `object` = venueResource
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with a new value, it creates a new venue resource`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val venue = "Conference"
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = UpdatePaperState(paper)
        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = venue,
            classes = setOf(Classes.venue),
            contributorId = command.contributorId
        )
        val venueId = ThingId("R456")

        every {
            resourceRepository.findAll(
                includeClasses = setOf(Classes.venue),
                label = any(),
                pageable = PageRequests.SINGLE
            )
        } returns Page.empty()
        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns venueId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasVenue,
                    objectId = venueId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            resourceRepository.findAll(
                includeClasses = setOf(Classes.venue),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBeEqualIgnoringCase venue
                },
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasVenue,
                    objectId = venueId
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication venue with a new value, it reuses an existing venue resource`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val venue = "Conference"
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = UpdatePaperState(paper)
        val venueResource = createResource(
            label = venue,
            classes = setOf(Classes.venue)
        )

        every {
            resourceRepository.findAll(
                includeClasses = setOf(Classes.venue),
                label = any(),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(venueResource)
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasVenue,
                    objectId = venueResource.id
                )
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasVenue,
                    objectId = venueResource.id
                )
            )
        } returns StatementId("S2")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            resourceRepository.findAll(
                includeClasses = setOf(Classes.venue),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBeEqualIgnoringCase venue
                },
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasVenue,
                    objectId = venueResource.id
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = null,
            publishedIn = null,
            url = ParsedIRI("https://orkg.org/")
        )
        val paper = createPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = updatePaperCommand().copy(
            publicationInfo = publicationInfo.toPublicationInfoDefinition()
        )
        val state = UpdatePaperState(paper)

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with null, it deletes the old literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = ParsedIRI("https://orkg.org/")
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val urlLiteral = createLiteral(label = paper.publicationInfo.url.toString())
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasURL),
                `object` = urlLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
    }

    @Test
    fun `Given a paper update command, when updating publication url with a new value, it creates a new literal`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val url = ParsedIRI("https://orkg.org/")
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = url
            )
        )
        val state = UpdatePaperState(paper)
        val urlLiteralId = ThingId("L4356")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns urlLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with a new value, it replaces the old statement`() {
        val paper = createPaper().copy(
            publicationInfo = PublicationInfo(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = ParsedIRI("https://example.org")
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = ParsedIRI("https://orkg.org")
            )
        )
        val oldUrlLiteral = createLiteral(
            label = paper.publicationInfo.url.toString(),
            datatype = Literals.XSD.URI.prefixedUri
        )
        val newUrlLiteralId = ThingId("L15436")
        val statementId = StatementId("S1")
        val statements = listOf(
            createStatement(
                id = statementId,
                subject = createResource(command.paperId),
                predicate = createPredicate(Predicates.hasURL),
                `object` = oldUrlLiteral
            )
        ).groupBy { it.subject.id }
        val state = UpdatePaperState(paper, statements)

        every { statementService.deleteAllById(setOf(statementId)) } just runs
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns newUrlLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasURL,
                    objectId = newUrlLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.publicationInfo!!.url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.paperId,
                    predicateId = Predicates.hasURL,
                    objectId = newUrlLiteralId
                )
            )
        }
    }

    private fun PublicationInfo.toPublicationInfoDefinition(): PublicationInfoDefinition =
        PublicationInfoDefinition(
            publishedMonth = publishedMonth,
            publishedYear = publishedYear,
            publishedIn = publishedIn?.label,
            url = url
        )
}
