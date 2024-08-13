package org.orkg.contenttypes.domain.actions.papers.snapshot

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorCreateValidator
import org.orkg.contenttypes.domain.actions.papers.PaperAuthorCreator
import org.orkg.contenttypes.domain.actions.papers.PaperIdentifierCreator
import org.orkg.contenttypes.domain.actions.papers.PaperMentioningsCreator
import org.orkg.contenttypes.domain.actions.papers.PaperPublicationInfoCreator
import org.orkg.contenttypes.domain.actions.papers.PaperResearchFieldCreator
import org.orkg.contenttypes.domain.actions.papers.PaperSDGCreator
import org.orkg.contenttypes.domain.actions.papers.snapshot.SnapshotPaperAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

class PaperSnapshotPaperCloner(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases
) : SnapshotPaperAction {
    override fun invoke(command: PublishPaperUseCase.PublishCommand, state: State): State {
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
            PaperSnapshotResourceCreator(resourceService),
            PaperIdentifierCreator(statementService, literalService),
            PaperSDGCreator(literalService, statementService),
            PaperMentioningsCreator(literalService, statementService),
            PaperAuthorCreator(resourceService, statementService, literalService, listService),
            PaperResearchFieldCreator(literalService, statementService),
            PaperPublicationInfoCreator(resourceService, resourceRepository, statementService, literalService)
        )
        return state.copy(paperVersionId = steps.execute(createPaperCommand, CreatePaperState()).paperId!!)
    }
}
