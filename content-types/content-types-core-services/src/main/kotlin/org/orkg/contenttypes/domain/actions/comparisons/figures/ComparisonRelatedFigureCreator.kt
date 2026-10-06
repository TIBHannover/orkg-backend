package org.orkg.contenttypes.domain.actions.comparisons.figures

import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.comparisons.figures.CreateComparisonRelatedFigureAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonRelatedFigureCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : CreateComparisonRelatedFigureAction {
    override fun invoke(command: CreateComparisonRelatedFigureCommand, state: State): State {
        val comparisonRelatedFigureId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedFigure),
            ),
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasRelatedFigure,
                objectId = comparisonRelatedFigureId,
                extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get from command
            ),
        )
        if (command.image != null) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparisonRelatedFigureId,
                    predicateId = Predicates.hasImage,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = command.contributorId,
                            label = command.image!!,
                            extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get extraction method from command
                        ),
                    ),
                    extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get extraction method from command
                ),
            )
        }
        if (command.description != null) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparisonRelatedFigureId,
                    predicateId = Predicates.description,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = command.contributorId,
                            label = command.description!!,
                            extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get extraction method from command
                        ),
                    ),
                    extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get extraction method from command
                ),
            )
        }
        return state.copy(comparisonRelatedFigureId = comparisonRelatedFigureId)
    }
}
