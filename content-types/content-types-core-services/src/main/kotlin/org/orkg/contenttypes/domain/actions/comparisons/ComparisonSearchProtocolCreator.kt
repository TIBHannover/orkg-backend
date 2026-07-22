package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonSearchProtocolCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val listUseCases: ListUseCases,
) : CreateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        listUseCases: ListUseCases,
    ) : this(
        SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
        unsafeLiteralUseCases,
        unsafeStatementUseCases,
        listUseCases,
    )

    override fun invoke(command: CreateComparisonCommand, state: State): State {
        command.searchProtocol.inclusionCriteria?.also { inclusionCriteria ->
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.inclusionCriteria,
                label = inclusionCriteria,
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
        command.searchProtocol.exclusionCriteria?.also { exclusionCriteria ->
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.exclusionCriteria,
                label = exclusionCriteria,
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
        if (command.searchProtocol.searchStrings.isNotEmpty()) {
            val elements = command.searchProtocol.searchStrings.map { searchString ->
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        label = searchString,
                        extractionMethod = command.extractionMethod,
                    ),
                )
            }
            val searchStringsListId = listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search strings",
                    elements = elements,
                    extractionMethod = command.extractionMethod,
                ),
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchStrings,
                    objectId = searchStringsListId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        if (command.searchProtocol.researchQuestions.isNotEmpty()) {
            val elements = command.searchProtocol.researchQuestions.map { researchQuestion ->
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        label = researchQuestion,
                        extractionMethod = command.extractionMethod,
                    ),
                )
            }
            val researchQuestionsListId = listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Research questions",
                    elements = elements,
                    extractionMethod = command.extractionMethod,
                ),
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.researchQuestions,
                    objectId = researchQuestionsListId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        command.searchProtocol.numberOfStudiesOriginallyReturned?.also { studiesReturned ->
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesOriginallyReturned,
                label = studiesReturned.toString(),
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
        command.searchProtocol.numberOfStudiesRetained?.also { studiesRetained ->
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.comparisonId!!,
                predicateId = Predicates.numberOfStudiesRetained,
                label = studiesRetained.toString(),
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod,
            )
        }
        if (command.searchProtocol.searchEngines.isNotEmpty()) {
            val searchEngineListId = listUseCases.create(
                CreateListUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "Search engines",
                    elements = command.searchProtocol.searchEngines,
                    extractionMethod = command.extractionMethod,
                ),
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.searchEngines,
                    objectId = searchEngineListId,
                    extractionMethod = command.extractionMethod,
                ),
            )
        }
        return state
    }
}
