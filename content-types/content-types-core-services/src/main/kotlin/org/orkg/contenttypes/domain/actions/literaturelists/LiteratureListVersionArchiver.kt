package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Sort

private val bundleConfiguration = BundleConfiguration(
    minLevel = null,
    maxLevel = 10,
    blacklist = listOf(Classes.researchField),
    whitelist = emptyList()
)

class LiteratureListVersionArchiver(
    private val statementService: StatementUseCases,
    private val literatureListPublishedRepository: LiteratureListPublishedRepository
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        val statementToPersist = statementService.fetchAsBundle(
            thingId = command.id,
            configuration = bundleConfiguration,
            includeFirst = true,
            sort = Sort.unsorted()
        ).bundle
        literatureListPublishedRepository.save(
            PublishedContentType(
                rootId = state.literatureListVersionId!!,
                subgraph = statementToPersist
            )
        )
        return state
    }
}
