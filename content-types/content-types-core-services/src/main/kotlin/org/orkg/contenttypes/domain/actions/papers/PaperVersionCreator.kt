package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.papers.PublishPaperAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperVersionCreator(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val listService: ListUseCases,
) : PublishPaperAction {
    override fun invoke(command: PublishPaperCommand, state: State): State {
        val paper = state.paper!!
        val createPaperCommand = CreatePaperCommand(
            contributorId = command.contributorId,
            title = paper.title,
            researchFields = paper.researchFields.ids,
            identifiers = paper.identifiers,
            publicationInfo = PublicationInfoDefinition(
                publishedIn = paper.publicationInfo.publishedIn?.label,
                publishedYear = paper.publicationInfo.publishedYear,
                publishedMonth = paper.publicationInfo.publishedMonth,
                url = paper.publicationInfo.url
            ),
            authors = command.authors,
            sustainableDevelopmentGoals = paper.sustainableDevelopmentGoals.ids,
            mentionings = paper.mentionings.ids,
            observatories = paper.observatories,
            organizations = paper.organizations,
            extractionMethod = paper.extractionMethod,
            contents = null
        )
        val steps = listOf(
            PaperAuthorCreateValidator(resourceRepository, statementRepository),
            PaperSnapshotResourceCreator(unsafeResourceUseCases),
            PaperIdentifierCreator(unsafeStatementUseCases, unsafeLiteralUseCases),
            PaperSDGCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            PaperMentioningsCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            PaperAuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
            PaperResearchFieldCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            PaperPublicationInfoCreator(unsafeResourceUseCases, resourceRepository, unsafeStatementUseCases, unsafeLiteralUseCases)
        )
        return state.copy(paperVersionId = steps.execute(createPaperCommand, CreatePaperState()).paperId!!)
    }
}
