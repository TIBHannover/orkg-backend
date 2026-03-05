package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListSnapshotV1
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.contenttypes.output.LiteratureListSnapshotRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Sort
import java.time.Clock
import java.time.OffsetDateTime

private val bundleConfiguration = BundleConfiguration(
    minLevel = null,
    maxLevel = 10,
    blacklist = listOf(Classes.researchField),
    whitelist = emptyList(),
)

class LiteratureListVersionArchiver(
    private val statementService: StatementUseCases,
    private val literatureListSnapshotRepository: LiteratureListSnapshotRepository,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    private val clock: Clock,
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        val statementsToPersist = statementService.fetchAsBundle(
            thingId = command.id,
            configuration = bundleConfiguration,
            includeFirst = true,
            sort = Sort.unsorted(),
        ).bundle
        literatureListSnapshotRepository.save(
            LiteratureListSnapshotV1(
                id = snapshotIdGenerator.nextIdentity(),
                createdBy = command.contributorId,
                createdAt = OffsetDateTime.now(clock),
                resourceId = state.literatureListVersionId!!,
                rootId = command.id,
                subgraph = statementsToPersist,
            ),
        )
        return state
    }
}
