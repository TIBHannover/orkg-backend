package org.orkg.contenttypes.domain.actions.comparisons.figures

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureState
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
import org.orkg.graph.testing.fixtures.createLiteral
import java.util.UUID

internal class ComparisonRelatedFigureCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val comparisonRelatedFigureCreator = ComparisonRelatedFigureCreator(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
    )

    @Test
    fun `Given a comparison related figure create command, it creates a comparison related figure`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = "https://example.org/test.png",
            description = "comparison related figure description",
            modifiable = true,
        )
        val figureId = ThingId("R456")
        val image = createLiteral(ThingId("L1"))
        val description = createLiteral(ThingId("L3"))
        val extractionMethod = ExtractionMethod.UNKNOWN
        val createImageLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.image!!,
            extractionMethod = extractionMethod,
            modifiable = command.modifiable,
        )
        val createDescriptionCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = command.contributorId,
            label = command.description!!,
            extractionMethod = extractionMethod,
            modifiable = command.modifiable,
        )
        val state = CreateComparisonRelatedFigureState()
        val expected = CreateComparisonRelatedFigureState(figureId)

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedFigure),
                    modifiable = command.modifiable,
                ),
            )
        } returns figureId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedFigure,
                    objectId = figureId,
                    modifiable = command.modifiable,
                ),
            )
        } returns StatementId("S1")
        every { unsafeLiteralUseCases.create(createImageLiteralCommand) } returns image.id
        every { unsafeLiteralUseCases.create(createDescriptionCommand) } returns description.id
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = figureId,
                    predicateId = Predicates.hasImage,
                    objectId = image.id,
                    modifiable = command.modifiable,
                ),
            )
        } returns StatementId("S2")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = figureId,
                    predicateId = Predicates.description,
                    objectId = description.id,
                    modifiable = command.modifiable,
                ),
            )
        } returns StatementId("S3")

        comparisonRelatedFigureCreator(command, state) shouldBe expected

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.label,
                    classes = setOf(Classes.comparisonRelatedFigure),
                    modifiable = command.modifiable,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.comparisonId,
                    predicateId = Predicates.hasRelatedFigure,
                    objectId = figureId,
                    modifiable = command.modifiable,
                ),
            )
        }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createImageLiteralCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createDescriptionCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = figureId,
                    predicateId = Predicates.hasImage,
                    objectId = image.id,
                    modifiable = command.modifiable,
                ),
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = figureId,
                    predicateId = Predicates.description,
                    objectId = description.id,
                    modifiable = command.modifiable,
                ),
            )
        }
    }
}
