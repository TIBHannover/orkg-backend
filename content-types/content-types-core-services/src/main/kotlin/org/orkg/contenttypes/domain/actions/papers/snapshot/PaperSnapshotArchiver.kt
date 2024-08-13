package org.orkg.contenttypes.domain.actions.papers.snapshot

import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.actions.papers.snapshot.SnapshotPaperAction.State
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.output.PaperPublishedRepository
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

class PaperSnapshotArchiver(
    private val statementService: StatementUseCases,
    private val paperPublishedRepository: PaperPublishedRepository
) : SnapshotPaperAction {
    override fun invoke(command: PublishPaperUseCase.PublishCommand, state: State): State {
        val statementsToPersist = state.paper!!.contributions.flatMap { (id, _) ->
            statementService.fetchAsBundle(
                thingId = id,
                configuration = bundleConfiguration,
                includeFirst = true,
                sort = Sort.unsorted()
            ).bundle
        }
        val publishedContentType = PublishedContentType(state.paperVersionId!!, statementsToPersist)
        paperPublishedRepository.save(publishedContentType)
        return state
    }
}
