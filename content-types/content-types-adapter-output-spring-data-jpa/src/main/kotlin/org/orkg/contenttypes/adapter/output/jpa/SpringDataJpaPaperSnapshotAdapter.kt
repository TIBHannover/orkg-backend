package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.jpa.internal.PaperSnapshotEntity
import org.orkg.contenttypes.adapter.output.jpa.internal.PostgresPaperSnapshotRepository
import org.orkg.contenttypes.domain.PaperSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.output.PaperSnapshotRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaPaperSnapshotAdapter(
    private val postgresPaperSnapshotRepository: PostgresPaperSnapshotRepository,
    private val objectMapper: ObjectMapper,
) : PaperSnapshotRepository {
    override fun save(snapshot: PaperSnapshot<*>) {
        postgresPaperSnapshotRepository.save(snapshot.toPaperSnapshotEntity())
    }

    override fun findById(id: SnapshotId): Optional<PaperSnapshot<*>> =
        postgresPaperSnapshotRepository.findById(id.value)
            .map { it.toPaperSnapshot(objectMapper) }

    override fun findByResourceId(id: ThingId): Optional<PaperSnapshot<*>> =
        postgresPaperSnapshotRepository.findByResourceId(id.value)
            .map { it.toPaperSnapshot(objectMapper) }

    override fun deleteAll() = postgresPaperSnapshotRepository.deleteAll()

    override fun count(): Long = postgresPaperSnapshotRepository.count()

    private fun PaperSnapshot<*>.toPaperSnapshotEntity(): PaperSnapshotEntity =
        postgresPaperSnapshotRepository.findById(id.value).orElseGet(::PaperSnapshotEntity).apply {
            id = this@toPaperSnapshotEntity.id.value
            createdBy = this@toPaperSnapshotEntity.createdBy.value
            createdAt = this@toPaperSnapshotEntity.createdAt
            createdAtOffsetTotalSeconds = this@toPaperSnapshotEntity.createdAt.offset.totalSeconds
            data = objectMapper.valueToTree(this@toPaperSnapshotEntity.data)
            modelVersion = this@toPaperSnapshotEntity.modelVersion
            resourceId = this@toPaperSnapshotEntity.resourceId.value
        }
}
