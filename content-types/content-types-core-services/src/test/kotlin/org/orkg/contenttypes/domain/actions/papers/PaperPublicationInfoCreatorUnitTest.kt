package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

internal class PaperPublicationInfoCreatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val paperPublicationInfoCreator = PaperPublicationInfoCreator(
        unsafeResourceUseCases = unsafeResourceUseCases,
        resourceRepository = resourceRepository,
        unsafeStatementUseCases = unsafeStatementUseCases,
        unsafeLiteralUseCases = unsafeLiteralUseCases
    )

    @Test
    fun `Given a paper create command, when linking empty publication info, it returns success`() {
        val paperId = ThingId("R123")
        val command = createPaperCommand().copy(
            publicationInfo = null
        )
        val state = CreatePaperState(
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
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )
        val monthLiteralId = ThingId("L1")

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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.monthPublished,
                    objectId = monthLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.monthPublished,
                    objectId = monthLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking publication year, it returns success`() {
        val paperId = ThingId("R123")
        val year = 5L
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = year,
                publishedIn = null,
                url = null
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )
        val yearLiteralId = ThingId("L1")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns yearLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.paperId!!,
                    predicateId = Predicates.yearPublished,
                    objectId = yearLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.paperId!!,
                    predicateId = Predicates.yearPublished,
                    objectId = yearLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking existing publication venue, it returns success`() {
        val paperId = ThingId("R123")
        val venue = "Conference"
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )
        val venueResource = createResource(label = venue)

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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasVenue,
                    objectId = venueResource.id
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasVenue,
                    objectId = venueResource.id
                )
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking non-existing publication venue, it returns success`() {
        val paperId = ThingId("R123")
        val venue = "Conference"
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )
        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = venue,
            classes = setOf(Classes.venue)
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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasVenue,
                    objectId = venueId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasVenue,
                    objectId = venueId
                )
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking publication url, it returns success`() {
        val paperId = ThingId("R123")
        val url = ParsedIRI("https://orkg.org")
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = url
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )
        val urlLiteralId = ThingId("L1")

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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        } returns StatementId("S1")

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
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
                    subjectId = state.paperId!!,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        }
    }
}
