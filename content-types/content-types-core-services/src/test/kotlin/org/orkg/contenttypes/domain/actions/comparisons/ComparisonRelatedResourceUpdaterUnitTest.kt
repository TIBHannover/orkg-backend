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
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparisonRelatedResource
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.UpdateComparisonUseCase.UpdateComparisonRelatedResourceCommand
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

class ComparisonRelatedResourceUpdaterUnitTest {
    private val comparisonService: ComparisonUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val contributionCreator = ComparisonRelatedResourceUpdater(
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
    fun `Given a comparison related resource update command, when contents are unchanged, it does nothing`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand()

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
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
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
    fun `Given a comparison related resource update command, when it does not exist, it throws an exception`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand()

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.empty()

        assertThrows<ComparisonRelatedResourceNotFound> { contributionCreator.execute(command) }

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        }
    }

    @Test
    fun `Given a comparison related resource update command, when comparison is not the current version, it throws an exception`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand()

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
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

        assertThrows<ComparisonRelatedResourceNotModifiable> { contributionCreator.execute(command) }

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
    fun `Given a comparison related resource update command, when label has changed, it updates the label`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand().copy(
            label = "updated label"
        )
        val updateResourceCommand = UpdateResourceUseCase.UpdateCommand(
            id = comparisonRelatedResource.id,
            label = command.label
        )

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
        every {
            statementService.findAll(
                subjectClasses = setOf(Classes.comparison),
                predicateId = Predicates.hasPreviousVersion,
                objectId = command.comparisonId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every { resourceService.update(updateResourceCommand) } just runs

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
        verify(exactly = 1) { resourceService.update(updateResourceCommand) }
    }

    @Test
    fun `Given a comparison related resource update command, when image has changed, it updates the image`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand().copy(
            image = "https://orkg.org/path/to/new/image.png"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        } just runs

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasImage,
                label = command.image
            )
        }
    }

    @Test
    fun `Given a comparison related resource update command, when url has changed, it updates the url`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand().copy(
            url = "https://orkg.org/path/to/new/url"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasURL,
                label = command.url
            )
        } just runs

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.hasURL,
                label = command.url
            )
        }
    }

    @Test
    fun `Given a comparison related resource update command, when description has changed, it updates the description`() {
        val comparisonRelatedResource = createDummyComparisonRelatedResource()
        val command = comparisonRelatedResource.toComparisonRelatedResourceUpdateCommand().copy(
            description = "updated description"
        )
        val statements = listOf(
            createStatement(StatementId("S123")),
            createStatement(StatementId("S456"))
        )

        every {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
            )
        } returns Optional.of(comparisonRelatedResource)
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.description,
                label = command.description
            )
        } just runs

        contributionCreator.execute(command)

        verify(exactly = 1) {
            comparisonService.findRelatedResourceById(
                comparisonId = command.comparisonId,
                id = command.comparisonRelatedResourceId
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
                subjectId = command.comparisonRelatedResourceId,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.comparisonRelatedResourceId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
    }

    private fun ComparisonRelatedResource.toComparisonRelatedResourceUpdateCommand(): UpdateComparisonRelatedResourceCommand =
        UpdateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId("0b3d7108-ea98-448f-85ef-e67a63a8b32b"),
            comparisonRelatedResourceId = id,
            label = label,
            image = image,
            url = url,
            description = description
        )
}
