package org.orkg.contenttypes.domain.actions.comparisons.resources

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceState
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import java.util.UUID

internal class ComparisonRelatedResourceCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val comparisonRelatedResourceCreator = ComparisonRelatedResourceCreator(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
    )

    @Test
    fun `Given a comparison related resource create command, it creates a comparison related resource`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description",
        )
        val resourceId = ThingId("R456")
        val comparison = createResource(classes = setOf(Classes.comparison))
        val imageLiteralId = ThingId("L1")
        val urlLiteralId = ThingId("L2")
        val descriptionLiteralId = ThingId("L3")
        val createImageLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.image!!,
            extractionMethod = ExtractionMethod.UNKNOWN,
        )
        val createUrlLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.url!!,
            extractionMethod = ExtractionMethod.UNKNOWN,
        )
        val createDescriptionLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.description!!,
            extractionMethod = ExtractionMethod.UNKNOWN,
        )
        val state = CreateComparisonRelatedResourceState()
        val expected = CreateComparisonRelatedResourceState(resourceId)

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                ),
            )
        } returns resourceId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedResource,
                    objectId = resourceId,
                ),
            )
        } returns StatementId("S123")
        every { unsafeLiteralUseCases.create(createImageLiteralCommand) } returns imageLiteralId
        every { unsafeLiteralUseCases.create(createUrlLiteralCommand) } returns urlLiteralId
        every { unsafeLiteralUseCases.create(createDescriptionLiteralCommand) } returns descriptionLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasImage,
                    objectId = imageLiteralId,
                ),
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId,
                ),
            )
        } returns StatementId("S2")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.description,
                    objectId = descriptionLiteralId,
                ),
            )
        } returns StatementId("S3")

        comparisonRelatedResourceCreator(command, state) shouldBe expected

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedResource),
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedResource,
                    objectId = resourceId,
                ),
            )
        }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createImageLiteralCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createUrlLiteralCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createDescriptionLiteralCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasImage,
                    objectId = imageLiteralId,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.hasURL,
                    objectId = urlLiteralId,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = resourceId,
                    predicateId = Predicates.description,
                    objectId = descriptionLiteralId,
                ),
            )
        }
    }
}
