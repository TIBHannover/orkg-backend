package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.jpa.internal.PostgresSmartReviewSnapshotRepository
import org.orkg.contenttypes.adapter.output.jpa.internal.SmartReviewSnapshotEntity
import org.orkg.contenttypes.domain.SmartReviewSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.output.SmartReviewSnapshotRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaSmartReviewSnapshotAdapter(
    private val postgresSmartReviewSnapshotRepository: PostgresSmartReviewSnapshotRepository,
    private val objectMapper: ObjectMapper,
) : SmartReviewSnapshotRepository {
    override fun save(snapshot: SmartReviewSnapshot<*>) {
        postgresSmartReviewSnapshotRepository.save(snapshot.toSmartReviewSnapshotEntity())
    }

    override fun findById(id: SnapshotId): Optional<SmartReviewSnapshot<*>> =
        postgresSmartReviewSnapshotRepository.findById(id.value)
            .map { it.toSmartReviewSnapshot(objectMapper) }

    override fun findByResourceId(id: ThingId): Optional<SmartReviewSnapshot<*>> =
        postgresSmartReviewSnapshotRepository.findByResourceId(id.value)
            .map { it.toSmartReviewSnapshot(objectMapper) }

    override fun deleteAll() = postgresSmartReviewSnapshotRepository.deleteAll()

    override fun count(): Long = postgresSmartReviewSnapshotRepository.count()

    private fun SmartReviewSnapshot<*>.toSmartReviewSnapshotEntity(): SmartReviewSnapshotEntity =
        postgresSmartReviewSnapshotRepository.findById(id.value).orElseGet(::SmartReviewSnapshotEntity).apply {
            id = this@toSmartReviewSnapshotEntity.id.value
            createdBy = this@toSmartReviewSnapshotEntity.createdBy.value
            createdAt = this@toSmartReviewSnapshotEntity.createdAt
            createdAtOffsetTotalSeconds = this@toSmartReviewSnapshotEntity.createdAt.offset.totalSeconds
            data = objectMapper.valueToTree(this@toSmartReviewSnapshotEntity.data)
            modelVersion = this@toSmartReviewSnapshotEntity.modelVersion
            resourceId = this@toSmartReviewSnapshotEntity.resourceId.value
            rootId = this@toSmartReviewSnapshotEntity.rootId.value
        }
}
