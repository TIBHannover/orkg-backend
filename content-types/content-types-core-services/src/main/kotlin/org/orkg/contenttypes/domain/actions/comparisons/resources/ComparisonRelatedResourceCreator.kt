package org.orkg.contenttypes.domain.actions.comparisons.resources

import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.comparisons.resources.CreateComparisonRelatedResourceAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonRelatedResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : CreateComparisonRelatedResourceAction {
    override fun invoke(command: CreateComparisonRelatedResourceCommand, state: State): State {
        val comparisonRelatedResourceId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.comparisonRelatedResource),
            ),
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasRelatedResource,
                objectId = comparisonRelatedResourceId,
                extractionMethod = ExtractionMethod.UNKNOWN, // TODO: Get extraction method from command
            ),
        )
        if (command.image != null) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparisonRelatedResourceId,
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
        if (command.url != null) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = comparisonRelatedResourceId,
                    predicateId = Predicates.hasURL,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = command.contributorId,
                            label = command.url!!,
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
                    subjectId = comparisonRelatedResourceId,
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
        return state.copy(comparisonRelatedResourceId = comparisonRelatedResourceId)
    }
}
