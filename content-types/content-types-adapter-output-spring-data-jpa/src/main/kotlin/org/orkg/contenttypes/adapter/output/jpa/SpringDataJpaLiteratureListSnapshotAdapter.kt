package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.jpa.internal.LiteratureListSnapshotEntity
import org.orkg.contenttypes.adapter.output.jpa.internal.PostgresLiteratureListSnapshotRepository
import org.orkg.contenttypes.domain.LiteratureListSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.output.LiteratureListSnapshotRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaLiteratureListSnapshotAdapter(
    private val postgresLiteratureListSnapshotRepository: PostgresLiteratureListSnapshotRepository,
    private val objectMapper: ObjectMapper,
) : LiteratureListSnapshotRepository {
    override fun save(snapshot: LiteratureListSnapshot<*>) {
        postgresLiteratureListSnapshotRepository.save(snapshot.toLiteratureListSnapshotEntity())
    }

    override fun findById(id: SnapshotId): Optional<LiteratureListSnapshot<*>> =
        postgresLiteratureListSnapshotRepository.findById(id.value)
            .map { it.toLiteratureListSnapshot(objectMapper) }

    override fun findByResourceId(id: ThingId): Optional<LiteratureListSnapshot<*>> =
        postgresLiteratureListSnapshotRepository.findByResourceId(id.value)
            .map { it.toLiteratureListSnapshot(objectMapper) }

    override fun deleteAll() = postgresLiteratureListSnapshotRepository.deleteAll()

    override fun count(): Long = postgresLiteratureListSnapshotRepository.count()

    private fun LiteratureListSnapshot<*>.toLiteratureListSnapshotEntity(): LiteratureListSnapshotEntity =
        postgresLiteratureListSnapshotRepository.findById(id.value).orElseGet(::LiteratureListSnapshotEntity).apply {
            id = this@toLiteratureListSnapshotEntity.id.value
            createdBy = this@toLiteratureListSnapshotEntity.createdBy.value
            createdAt = this@toLiteratureListSnapshotEntity.createdAt
            createdAtOffsetTotalSeconds = this@toLiteratureListSnapshotEntity.createdAt.offset.totalSeconds
            data = objectMapper.valueToTree(this@toLiteratureListSnapshotEntity.data)
            modelVersion = this@toLiteratureListSnapshotEntity.modelVersion
            resourceId = this@toLiteratureListSnapshotEntity.resourceId.value
            rootId = this@toLiteratureListSnapshotEntity.rootId.value
        }
}
