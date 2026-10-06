package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureState
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceState
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.domain.actions.comparisons.figures.ComparisonRelatedFigureCreator
import org.orkg.contenttypes.domain.actions.comparisons.resources.ComparisonRelatedResourceCreator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
import org.orkg.contenttypes.input.ComparisonSearchProtocolCommand
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import java.time.Clock
import kotlin.jvm.optionals.getOrNull

class ComparisonVersionCreator(
    private val comparisonRelatedFigureUseCases: ComparisonRelatedFigureUseCases,
    private val comparisonRelatedResourceUseCases: ComparisonRelatedResourceUseCases,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val listService: ListUseCases,
    private val clock: Clock,
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparison = state.comparison!!
        val createComparisonCommand = CreateComparisonCommand(
            contributorId = command.contributorId,
            type = comparison.type,
            title = comparison.title,
            description = comparison.description.orEmpty(),
            researchFields = comparison.researchFields.ids,
            authors = comparison.authors,
            searchProtocol = ComparisonSearchProtocolCommand(
                inclusionCriteria = comparison.searchProtocol.inclusionCriteria,
                exclusionCriteria = comparison.searchProtocol.exclusionCriteria,
                searchEngines = comparison.searchProtocol.searchEngines.map { it.id },
                searchStrings = comparison.searchProtocol.searchStrings,
                researchQuestions = comparison.searchProtocol.researchQuestions,
                numberOfStudiesOriginallyReturned = comparison.searchProtocol.numberOfStudiesOriginallyReturned,
                numberOfStudiesRetained = comparison.searchProtocol.numberOfStudiesRetained,
            ),
            sustainableDevelopmentGoals = comparison.sustainableDevelopmentGoals.ids,
            sources = comparison.sources,
            visualizations = comparison.visualizations.ids,
            references = comparison.references,
            observatories = comparison.observatories,
            organizations = comparison.organizations,
            isAnonymized = comparison.isAnonymized,
            extractionMethod = comparison.extractionMethod,
        )
        val steps = listOf(
            ComparisonAuthorListCreateValidator(resourceRepository, statementRepository),
            ComparisonVersionResourceCreator(unsafeResourceUseCases),
            ComparisonDescriptionCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
            ComparisonSearchProtocolCreator(unsafeLiteralUseCases, unsafeStatementUseCases, listService),
            ComparisonSDGCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonResearchFieldCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonReferencesCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonIsAnonymizedCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            ComparisonContributionCreator(unsafeStatementUseCases),
            ComparisonVisualizationCreator(unsafeStatementUseCases),
            ComparisonPublicationInfoCreator(unsafeStatementUseCases, unsafeLiteralUseCases, clock),
        )
        val comparisonVersionId = steps.execute(createComparisonCommand, CreateComparisonState()).comparisonId!!
        comparison.relatedFigures.forEach {
            val relatedFigure = comparisonRelatedFigureUseCases.findByIdAndComparisonId(comparison.id, it.id).getOrNull()
                ?: return@forEach
            val createComparisonRelatedFigureCommand = CreateComparisonRelatedFigureCommand(
                comparisonId = comparisonVersionId,
                contributorId = relatedFigure.createdBy,
                label = relatedFigure.label,
                image = relatedFigure.image,
                description = relatedFigure.description,
                modifiable = false,
            )
            val steps = listOf(
                ComparisonRelatedFigureCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            )
            steps.execute(createComparisonRelatedFigureCommand, CreateComparisonRelatedFigureState())
        }
        comparison.relatedResources.forEach {
            val relatedResource = comparisonRelatedResourceUseCases.findByIdAndComparisonId(comparison.id, it.id).getOrNull()
                ?: return@forEach
            val createComparisonRelatedResourceCommand = CreateComparisonRelatedResourceCommand(
                comparisonId = comparisonVersionId,
                contributorId = relatedResource.createdBy,
                label = relatedResource.label,
                image = relatedResource.image,
                url = relatedResource.url,
                description = relatedResource.description,
                modifiable = false,
            )
            val steps = listOf(
                ComparisonRelatedResourceCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            )
            steps.execute(createComparisonRelatedResourceCommand, CreateComparisonRelatedResourceState())
        }
        return state.copy(comparisonVersionId = comparisonVersionId)
    }
}
