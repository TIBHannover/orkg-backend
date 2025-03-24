package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.papers.PublishPaperAction.State
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.net.URI

class PaperVersionDoiPublisher(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
    private val doiService: DoiService,
    private val paperPublishBaseUri: String,
) : PublishPaperAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        doiService: DoiService,
        paperPublishBaseUri: String,
    ) : this(
        SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
        doiService,
        paperPublishBaseUri
    )

    override fun invoke(command: PublishPaperCommand, state: State): State {
        val paper = state.paper!!
        val paperVersionId = state.paperVersionId!!
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = paperVersionId.value,
                title = paper.title,
                subject = command.subject,
                description = command.description,
                url = URI.create("$paperPublishBaseUri/").resolve(paperVersionId.value),
                creators = paper.authors,
                resourceType = Classes.paper.value,
                resourceTypeGeneral = "Dataset",
                relatedIdentifiers = emptyList()
            )
        )
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = paperVersionId,
            predicateId = Predicates.hasDOI,
            label = doi.value
        )
        return state
    }
}
