package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.SingleListPropertyUpdater
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonSearchProtocolUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
    private val singleListPropertyUpdater: SingleListPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        listUseCases: ListUseCases,
    ) : this(
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
        SingleListPropertyUpdater(unsafeLiteralUseCases, unsafeStatementUseCases, listUseCases),
    )

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        command.searchProtocol?.also { searchProtocol ->
            val directStatements = state.statements[command.comparisonId].orEmpty()
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = directStatements,
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.inclusionCriteria,
                label = searchProtocol.inclusionCriteria,
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = directStatements,
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.exclusionCriteria,
                label = searchProtocol.exclusionCriteria,
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleListPropertyUpdater.updateLiteralListProperty(
                statements = state.statements,
                contributorId = command.contributorId,
                label = "Search strings",
                subjectId = command.comparisonId,
                predicateId = Predicates.searchStrings,
                objects = searchProtocol.searchStrings,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleListPropertyUpdater.updateLiteralListProperty(
                statements = state.statements,
                contributorId = command.contributorId,
                label = "Research questions",
                subjectId = command.comparisonId,
                predicateId = Predicates.researchQuestions,
                objects = searchProtocol.researchQuestions,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = directStatements,
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.numberOfStudiesOriginallyReturned,
                label = searchProtocol.numberOfStudiesOriginallyReturned?.toString(),
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = directStatements,
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.numberOfStudiesRetained,
                label = searchProtocol.numberOfStudiesRetained?.toString(),
                datatype = Literals.XSD.INT.prefixedUri,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
            singleListPropertyUpdater.updateListProperty(
                statements = state.statements,
                contributorId = command.contributorId,
                label = "Search engines",
                subjectId = command.comparisonId,
                predicateId = Predicates.searchEngines,
                objectIds = searchProtocol.searchEngines,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
        }
        return state
    }
}
