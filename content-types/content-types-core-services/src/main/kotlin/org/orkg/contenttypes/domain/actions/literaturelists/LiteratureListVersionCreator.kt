package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class LiteratureListVersionCreator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        val literatureList = state.literatureList!!
        val createLiteratureListCommand = CreateLiteratureListCommand(
            contributorId = command.contributorId,
            title = literatureList.title,
            researchFields = literatureList.researchFields.ids,
            authors = literatureList.authors,
            sustainableDevelopmentGoals = literatureList.sustainableDevelopmentGoals.ids,
            observatories = literatureList.observatories,
            organizations = literatureList.organizations,
            extractionMethod = literatureList.extractionMethod,
            sections = emptyList()
        )
        val steps = listOf(
            LiteratureListAuthorCreateValidator(resourceRepository, statementRepository),
            LiteratureListVersionResourceCreator(unsafeResourceUseCases),
            LiteratureListResearchFieldCreator(literalService, unsafeStatementUseCases),
            LiteratureListAuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, literalService, listService),
            LiteratureListSDGCreator(literalService, unsafeStatementUseCases)
        )
        return state.copy(
            literatureListVersionId = steps.execute(
                createLiteratureListCommand,
                CreateLiteratureListState()
            ).literatureListId!!
        )
    }
}
