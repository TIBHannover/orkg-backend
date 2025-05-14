package org.orkg.contenttypes.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional
import java.util.UUID

internal class ComparisonRelatedResourceServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val service = ComparisonRelatedResourceService(
        resourceRepository,
        statementRepository,
        resourceService,
        unsafeResourceUseCases,
        statementService,
        unsafeStatementUseCases,
        unsafeLiteralUseCases
    )

    @Test
    fun `Given a comparison related resource create command, it creates the comparison related resource`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )
        val resourceId = ThingId("R456")
        val comparison = createResource(classes = setOf(Classes.comparison))
        val imageLiteralId = ThingId("L1")
        val urlLiteralId = ThingId("L2")
        val descriptionLiteralId = ThingId("L3")

        every { resourceRepository.findById(command.comparisonId) } returns Optional.of(comparison)
        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                )
            )
        } returns resourceId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedResource,
                    objectId = resourceId
                )
            )
        } returns StatementId("S123")
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.image!!
                )
            )
        } returns imageLiteralId
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.url!!
                )
            )
        } returns urlLiteralId
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.description!!
                )
            )
        } returns descriptionLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasImage,
                    objectId = imageLiteralId
                )
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        } returns StatementId("S2")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.description,
                    objectId = descriptionLiteralId
                )
            )
        } returns StatementId("S3")

        service.create(command) shouldBe resourceId

        verify(exactly = 1) { resourceRepository.findById(command.comparisonId) }
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedResource,
                    objectId = resourceId
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.image!!
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.url!!
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.description!!
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasImage,
                    objectId = imageLiteralId
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.description,
                    objectId = descriptionLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a comparison related resource create command, when label is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "\n",
            image = null,
            url = null,
            description = null
        )
        shouldThrow<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `Given a comparison related resource create command, when comparison does not exist, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = null,
            url = null,
            description = null
        )

        every { resourceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.create(command) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }
}
