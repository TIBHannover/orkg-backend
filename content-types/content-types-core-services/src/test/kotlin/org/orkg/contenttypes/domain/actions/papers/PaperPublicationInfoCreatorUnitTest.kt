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
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

internal class PaperPublicationInfoCreatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val paperPublicationInfoCreator = PaperPublicationInfoCreator(
        unsafeResourceUseCases = unsafeResourceUseCases,
        resourceRepository = resourceRepository,
        statementService = statementService,
        literalService = literalService
    )

    @Test
    fun `Given a paper create command, when linking empty publication info, it returns success`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand().copy(
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
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
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
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns monthLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.monthPublished,
                `object` = monthLiteralId
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = month.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.monthPublished,
                `object` = monthLiteralId
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking publication year, it returns success`() {
        val paperId = ThingId("R123")
        val year = 5L
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
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
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns yearLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.yearPublished,
                `object` = yearLiteralId
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.yearPublished,
                `object` = yearLiteralId
            )
        }
    }

    @Test
    fun `Given a paper create command, when linking existing publication venue, it returns success`() {
        val paperId = ThingId("R123")
        val venue = "Conference"
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
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
            publicationInfo = PublicationInfoDefinition(
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
        val url = ParsedIRI("https://orkg.org")
        val command = dummyCreatePaperCommand().copy(
            publicationInfo = PublicationInfoDefinition(
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
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns urlLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasURL,
                `object` = urlLiteralId
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
                CreateCommand(
                    contributorId = command.contributorId,
                    label = url.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.paperId!!,
                predicate = Predicates.hasURL,
                `object` = urlLiteralId
            )
        }
    }
}
