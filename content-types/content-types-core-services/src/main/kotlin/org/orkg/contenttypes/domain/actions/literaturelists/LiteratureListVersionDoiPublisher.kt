package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.net.URI

class LiteratureListVersionDoiPublisher(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
    private val doiService: DoiService,
    private val literatureListPublishBaseUri: String,
) : PublishLiteratureListAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        doiService: DoiService,
        literatureListPublishBaseUri: String,
    ) : this(
        SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
        doiService,
        literatureListPublishBaseUri,
    )

    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        if (!command.assignDOI) {
            return state
        }
        val literatureList = state.literatureList!!
        val literatureListVersionId = state.literatureListVersionId!!
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = literatureListVersionId.value,
                title = literatureList.title,
                subject = literatureList.researchFields.firstOrNull()?.label.orEmpty(),
                description = command.description!!,
                url = URI.create("$literatureListPublishBaseUri/").resolve(literatureListVersionId.value),
                creators = literatureList.authors,
                resourceType = "Collection",
                resourceTypeGeneral = "Collection",
                relatedIdentifiers = emptyList(),
            ),
        )
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = literatureListVersionId,
            predicateId = Predicates.hasDOI,
            label = doi.value,
            extractionMethod = ExtractionMethod.UNKNOWN,
        )
        return state
    }
}
