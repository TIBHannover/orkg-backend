package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperSnapshot.ModelVersion
import org.orkg.graph.domain.GeneralStatement
import java.time.OffsetDateTime

sealed interface PaperSnapshot<T : Any> : Snapshot<T, ModelVersion> {
    val resourceId: ThingId
    val subgraph: List<GeneralStatement>

    enum class ModelVersion {
        V1,
    }
}

data class PaperSnapshotV1(
    override val id: SnapshotId,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val resourceId: ThingId,
    override val subgraph: List<GeneralStatement>,
) : PaperSnapshot<List<GeneralStatement>> {
    override val data: List<GeneralStatement> = subgraph
    override val modelVersion: ModelVersion = ModelVersion.V1
}
