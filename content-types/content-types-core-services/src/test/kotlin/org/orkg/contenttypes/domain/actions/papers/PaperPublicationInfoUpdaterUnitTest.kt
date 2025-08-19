package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.contenttypes.input.testing.fixtures.updatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperPublicationInfoUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val publicationInfoCreator: PublicationInfoCreator = mockk()

    private val paperPublicationInfoUpdater = PaperPublicationInfoUpdater(statementService, publicationInfoCreator)

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
            publicationInfo = publicationInfo.toPublicationInfoCommand()
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
            publicationInfo = PublicationInfoCommand(
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
            publicationInfo = PublicationInfoCommand(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(paper)

        every {
            publicationInfoCreator.linkPublicationMonth(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedMonth = month
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationMonth(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedMonth = month
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
            publicationInfo = PublicationInfoCommand(
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
            publicationInfoCreator.linkPublicationMonth(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedMonth = command.publicationInfo!!.publishedMonth!!
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationMonth(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedMonth = command.publicationInfo!!.publishedMonth!!
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
            publicationInfo = publicationInfo.toPublicationInfoCommand()
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
            publicationInfo = PublicationInfoCommand(
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
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = year,
                publishedIn = null,
                url = null
            )
        )
        val state = UpdatePaperState(paper)

        every {
            publicationInfoCreator.linkPublicationYear(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedYear = year
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationYear(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedYear = year
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
            publicationInfo = PublicationInfoCommand(
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
            publicationInfoCreator.linkPublicationYear(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedYear = command.publicationInfo!!.publishedYear!!
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationYear(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedYear = command.publicationInfo!!.publishedYear!!
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
            publicationInfo = publicationInfo.toPublicationInfoCommand()
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
            publicationInfo = PublicationInfoCommand(
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
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = venue,
                url = null
            )
        )
        val state = UpdatePaperState(paper)

        every {
            publicationInfoCreator.linkPublicationVenue(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedIn = venue
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationVenue(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                publishedIn = venue
            )
        }
    }

    @Test
    fun `Given a paper update command, when updating publication url with the same value, it does nothing`() {
        val publicationInfo = PublicationInfo(
            publishedMonth = null,
            publishedYear = null,
            publishedIn = null,
            url = ParsedIRI.create("https://orkg.org/")
        )
        val paper = createPaper().copy(
            publicationInfo = publicationInfo
        )
        val command = updatePaperCommand().copy(
            publicationInfo = publicationInfo.toPublicationInfoCommand()
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
                url = ParsedIRI.create("https://orkg.org/")
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
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
        val url = ParsedIRI.create("https://orkg.org/")
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = url
            )
        )
        val state = UpdatePaperState(paper)

        every {
            publicationInfoCreator.linkPublicationUrl(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                url = url
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationUrl(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                url = url
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
                url = ParsedIRI.create("https://example.org")
            )
        )
        val command = updatePaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = null,
                publishedYear = null,
                publishedIn = null,
                url = ParsedIRI.create("https://orkg.org")
            )
        )
        val oldUrlLiteral = createLiteral(
            label = paper.publicationInfo.url.toString(),
            datatype = Literals.XSD.URI.prefixedUri
        )
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
            publicationInfoCreator.linkPublicationUrl(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                url = command.publicationInfo!!.url!!
            )
        } just runs

        val result = paperPublicationInfoUpdater(command, state)

        result.asClue {
            it.paper shouldBe paper
            it.authors.size shouldBe 0
        }

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementId)) }
        verify(exactly = 1) {
            publicationInfoCreator.linkPublicationUrl(
                contributorId = command.contributorId,
                subjectId = command.paperId,
                url = command.publicationInfo!!.url!!
            )
        }
    }

    private fun PublicationInfo.toPublicationInfoCommand(): PublicationInfoCommand =
        PublicationInfoCommand(
            publishedMonth = publishedMonth,
            publishedYear = publishedYear,
            publishedIn = publishedIn?.label,
            url = url
        )
}
