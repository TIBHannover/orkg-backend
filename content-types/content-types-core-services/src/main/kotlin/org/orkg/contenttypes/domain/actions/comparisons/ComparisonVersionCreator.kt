package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class ComparisonVersionCreator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
    private val comparisonPublishedRepository: ComparisonPublishedRepository
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
            references = comparison.references,
            observatories = comparison.observatories,
            organizations = comparison.organizations,
            isAnonymized = comparison.isAnonymized,
            extractionMethod = comparison.extractionMethod
        )
        val steps = listOf(
            ComparisonAuthorCreateValidator(resourceRepository, statementRepository),
            ComparisonVersionResourceCreator(unsafeResourceUseCases),
            ComparisonDescriptionCreator(literalService, statementService),
            ComparisonAuthorCreator(unsafeResourceUseCases, statementService, literalService, listService),
            ComparisonSDGCreator(literalService, statementService),
            ComparisonResearchFieldCreator(literalService, statementService),
            ComparisonReferencesCreator(literalService, statementService),
            ComparisonIsAnonymizedCreator(literalService, statementService),
            ComparisonContributionCreator(statementService),
            ComparisonVersionTableCreator(comparisonPublishedRepository),
            ComparisonPublicationInfoCreator(statementService, literalService)
        )
        return state.copy(
            comparisonVersionId = steps.execute(
                createComparisonCommand,
                CreateComparisonState()
            ).comparisonId!!
        )
    }
}
