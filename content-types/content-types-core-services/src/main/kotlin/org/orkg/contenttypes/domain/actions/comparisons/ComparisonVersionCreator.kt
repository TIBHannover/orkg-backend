package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonVersionCreator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val listService: ListUseCases,
    private val comparisonPublishedRepository: ComparisonPublishedRepository,
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparison = state.comparison!!
        val createComparisonCommand = CreateComparisonCommand(
            contributorId = command.contributorId,
            title = comparison.title,
            description = comparison.description.orEmpty(),
            researchFields = comparison.researchFields.ids,
            authors = comparison.authors,
            sustainableDevelopmentGoals = comparison.sustainableDevelopmentGoals.ids,
            contributions = comparison.contributions.ids,
            config = state.config!!,
            data = state.data!!,
            visualizations = comparison.visualizations.ids,
            references = comparison.references,
            observatories = comparison.observatories,
            organizations = comparison.organizations,
            isAnonymized = comparison.isAnonymized,
            extractionMethod = comparison.extractionMethod
        )
        val steps = listOf(
            ComparisonAuthorListCreateValidator(resourceRepository, statementRepository),
            ComparisonVersionResourceCreator(unsafeResourceUseCases),
            ComparisonDescriptionCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
            ComparisonSDGCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonResearchFieldCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonReferencesCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonIsAnonymizedCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonContributionCreator(unsafeStatementUseCases),
            ComparisonVisualizationCreator(unsafeStatementUseCases),
            ComparisonVersionTableCreator(comparisonPublishedRepository),
            ComparisonPublicationInfoCreator(unsafeStatementUseCases, unsafeLiteralUseCases)
        )
        return state.copy(
            comparisonVersionId = steps.execute(
                createComparisonCommand,
                CreateComparisonState()
            ).comparisonId!!
        )
    }
}
