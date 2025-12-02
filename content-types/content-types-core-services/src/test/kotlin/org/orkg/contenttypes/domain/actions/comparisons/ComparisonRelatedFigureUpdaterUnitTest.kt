package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedFigure
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.Optional

internal class ComparisonRelatedFigureUpdaterUnitTest : MockkBaseTest {
    private val comparisonRelatedFigureUseCases: ComparisonRelatedFigureUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val comparisonRelatedFigureUpdater = ComparisonRelatedFigureUpdater(
        comparisonRelatedFigureUseCases,
        resourceService,
        statementService,
        singleStatementPropertyUpdater
    )

    @Test
    fun `Given a comparison related figure update command, when contents are unchanged, it does nothing`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()
        val comparison = createResource(classes = setOf(Classes.comparison))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)

        comparisonRelatedFigureUpdater.execute(command)

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
        verify(exactly = 1) {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when image is invalid, it throws an exception`() {
        val command = createComparisonRelatedFigure()
            .toComparisonRelatedFigureUpdateCommand()
            .copy(image = "\n")

        shouldThrow<InvalidLabel> { comparisonRelatedFigureUpdater.execute(command) }.asClue {
            it.property shouldBe "image"
        }
    }

    @Test
    fun `Given a comparison related figure update command, when description is invalid, it throws an exception`() {
        val command = createComparisonRelatedFigure()
            .toComparisonRelatedFigureUpdateCommand()
            .copy(description = "a".repeat(MAX_LABEL_LENGTH + 1))

        shouldThrow<InvalidDescription> { comparisonRelatedFigureUpdater.execute(command) }.asClue {
            it.property shouldBe "description"
        }
    }

    @Test
    fun `Given a comparison related figure update command, when comparison does not exist, it throws an exception`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()

        every { resourceService.findById(command.comparisonId) } returns Optional.empty()

        assertThrows<ComparisonNotFound> { comparisonRelatedFigureUpdater.execute(command) }

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
    }

    @Test
    fun `Given a comparison related figure update command, when it does not exist, it throws an exception`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()
        val comparison = createResource(classes = setOf(Classes.comparison))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.empty()

        assertThrows<ComparisonRelatedFigureNotFound> { comparisonRelatedFigureUpdater.execute(command) }

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
        verify(exactly = 1) {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when comparison is published, it throws an exception`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()
        val comparison = createResource(classes = setOf(Classes.comparisonPublished))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)

        assertThrows<ComparisonRelatedFigureNotModifiable> { comparisonRelatedFigureUpdater.execute(command) }

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
    }

    @Test
    fun `Given a comparison related figure update command, when label has changed, it updates the label`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            label = "updated label"
        )
        val updateFigureCommand = UpdateResourceUseCase.UpdateCommand(
            id = comparisonRelatedFigure.id,
            contributorId = command.contributorId,
            label = command.label
        )
        val comparison = createResource(classes = setOf(Classes.comparison))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every { resourceService.update(updateFigureCommand) } just runs

        comparisonRelatedFigureUpdater.execute(command)

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
        verify(exactly = 1) {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) { resourceService.update(updateFigureCommand) }
    }

    @Test
    fun `Given a comparison related figure update command, when image has changed, it updates the image`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            image = "https://orkg.org/path/to/new/image.png"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )
        val comparison = createResource(classes = setOf(Classes.comparison))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectId = command.comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        } just runs

        comparisonRelatedFigureUpdater.execute(command)

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
        verify(exactly = 1) {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = command.comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when description has changed, it updates the description`() {
        val comparisonRelatedFigure = createComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            description = "updated description"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )
        val comparison = createResource(classes = setOf(Classes.comparison))

        every { resourceService.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectId = command.comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.description,
                label = command.description
            )
        } just runs

        comparisonRelatedFigureUpdater.execute(command)

        verify(exactly = 1) { resourceService.findById(command.comparisonId) }
        verify(exactly = 1) {
            comparisonRelatedFigureUseCases.findByIdAndComparisonId(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectId = command.comparisonRelatedFigureId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedFigureId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
    }

    private fun ComparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand() =
        UpdateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId("0b3d7108-ea98-448f-85ef-e67a63a8b32b"),
            comparisonRelatedFigureId = id,
            label = label,
            image = image,
            description = description
        )
}
