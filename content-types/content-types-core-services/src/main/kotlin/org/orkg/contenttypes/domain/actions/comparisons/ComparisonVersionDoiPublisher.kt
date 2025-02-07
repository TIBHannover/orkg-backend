package org.orkg.contenttypes.domain.actions.comparisons

import java.net.URI
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonVersionDoiPublisher(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
    private val comparisonRepository: ComparisonRepository,
    private val doiService: DoiService,
    private val comparisonPublishBaseUri: String
) : PublishComparisonAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        comparisonRepository: ComparisonRepository,
        doiService: DoiService,
        comparisonPublishBaseUri: String
    ) : this(
        SingleStatementPropertyCreator(literalService, unsafeStatementUseCases),
        comparisonRepository,
        doiService,
        comparisonPublishBaseUri
    )

    override fun invoke(command: PublishComparisonCommand, state: State): State {
        if (!command.assignDOI) {
            return state
        }
        val comparison = state.comparison!!
        val comparisonVersionId = state.comparisonVersionId!!
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = comparisonVersionId.value,
                title = comparison.title,
                subject = command.subject,
                description = command.description,
                url = URI.create("$comparisonPublishBaseUri/").resolve(comparisonVersionId.value),
                creators = command.authors,
                resourceType = Classes.comparison.value,
                resourceTypeGeneral = "Dataset",
                relatedIdentifiers = comparisonRepository.findAllDOIsRelatedToComparison(comparison.id).toList()
            )
        )
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = comparisonVersionId,
            predicateId = Predicates.hasDOI,
            label = doi.value
        )
        return state
    }
}
