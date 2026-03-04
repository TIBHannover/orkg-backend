package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.SmartReviewSnapshotV1
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.contenttypes.output.SmartReviewSnapshotRepository
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
    whitelist = emptyList()
)

class SmartReviewVersionArchiver(
    private val statementService: StatementUseCases,
    private val smartReviewPublishedRepository: SmartReviewSnapshotRepository,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    private val clock: Clock,
) : PublishSmartReviewAction {
    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        val statementsToPersist = statementService.fetchAsBundle(
            thingId = command.smartReviewId,
            configuration = bundleConfiguration,
            includeFirst = true,
            sort = Sort.unsorted()
        ).bundle
        smartReviewPublishedRepository.save(
            SmartReviewSnapshotV1(
                id = snapshotIdGenerator.nextIdentity(),
                createdBy = command.contributorId,
                createdAt = OffsetDateTime.now(clock),
                resourceId = state.smartReviewVersionId!!,
                rootId = command.smartReviewId,
                subgraph = statementsToPersist,
            )
        )
        return state
    }
}
