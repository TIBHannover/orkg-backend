package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparisonRelatedFigure
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedFigureCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class ComparisonRelatedFigureUpdaterUnitTest {
    private val comparisonService: ComparisonUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val contributionCreator = ComparisonRelatedFigureUpdater(
        comparisonService, resourceService, statementService, singleStatementPropertyUpdater
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(comparisonService, resourceService, statementService, singleStatementPropertyUpdater)
    }

    @Test
    fun `Given a comparison related figure update command, when contents are unchanged, it does nothing`() {
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when it does not exist, it throws an exception`() {
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.empty()

        assertThrows<ComparisonRelatedFigureNotFound> { contributionCreator.execute(command) }

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when comparison is not the current version, it throws an exception`() {
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand()

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                subject = createResource(classes = setOf(Classes.comparison)),
                predicate = createPredicate(Predicates.hasPreviousVersion),
                `object` = createResource(command.comparisonId, classes = setOf(Classes.comparison))
            )
        )

        assertThrows<ComparisonRelatedFigureNotModifiable> { contributionCreator.execute(command) }

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a comparison related figure update command, when label has changed, it updates the label`() {
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            label = "updated label"
        )
        val updateFigureCommand = UpdateResourceUseCase.UpdateCommand(
            id = comparisonRelatedFigure.id,
            label = command.label
        )

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every { resourceService.update(updateFigureCommand) } just runs

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { resourceService.update(updateFigureCommand) }
    }

    @Test
    fun `Given a comparison related figure update command, when image has changed, it updates the image`() {
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            image = "https://orkg.org/path/to/new/image.png"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
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

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
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
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()
        val command = comparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand().copy(
            description = "updated description"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )

        every {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        } returns Optional.of(comparisonRelatedFigure)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
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

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedFigureById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedFigureId
            )
        }
        verify(exactly = 1) {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
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

    private fun ComparisonRelatedFigure.toComparisonRelatedFigureUpdateCommand(): UpdateComparisonRelatedFigureCommand =
        UpdateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId("0b3d7108-ea98-448f-85ef-e67a63a8b32b"),
            comparisonRelatedFigureId = id,
            label = label,
            image = image,
            description = description
        )
}
