package org.orkg.contenttypes.adapter.output.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.ThingId
import org.orkg.common.withDefaultSort
import org.orkg.contenttypes.adapter.output.jpa.internal.PostgresTemplateBasedResourceSnapshotRepository
import org.orkg.contenttypes.adapter.output.jpa.internal.TemplateBasedResourceSnapshotEntity
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot
import org.orkg.contenttypes.output.TemplateBasedResourceSnapshotRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaTemplateBasedResourceSnapshotAdapter(
    private val postgresTemplateBasedResourceSnapshotRepository: PostgresTemplateBasedResourceSnapshotRepository,
    private val objectMapper: ObjectMapper,
) : TemplateBasedResourceSnapshotRepository {
    override fun save(snapshot: TemplateBasedResourceSnapshot<*>) {
        postgresTemplateBasedResourceSnapshotRepository.save(snapshot.toTemplateBasedResourceSnapshotEntity())
    }

    override fun findById(id: SnapshotId): Optional<TemplateBasedResourceSnapshot<*>> =
        postgresTemplateBasedResourceSnapshotRepository.findById(id.value)
            .map { it.toTemplateBasedResourceSnapshot(objectMapper) }

    override fun findAllByResourceId(resourceId: ThingId, pageable: Pageable): Page<TemplateBasedResourceSnapshot<*>> =
        postgresTemplateBasedResourceSnapshotRepository.findAllByResourceId(
            resourceId = resourceId.value,
            pageable = pageable.withDefaultSort { Sort.by("createdAt") } // FIXME: Sorting properties should be snake_case
        ).map { it.toTemplateBasedResourceSnapshot(objectMapper) }

    override fun findAllByResourceIdAndTemplateId(
        resourceId: ThingId,
        templateId: ThingId,
        pageable: Pageable,
    ): Page<TemplateBasedResourceSnapshot<*>> =
        postgresTemplateBasedResourceSnapshotRepository.findAllByResourceIdAndTemplateId(
            resourceId = resourceId.value,
            templateId = templateId.value,
            pageable = pageable.withDefaultSort { Sort.by("createdAt") } // FIXME: Sorting properties should be snake_case
        ).map { it.toTemplateBasedResourceSnapshot(objectMapper) }

    override fun deleteAll() = postgresTemplateBasedResourceSnapshotRepository.deleteAll()

    override fun count(): Long = postgresTemplateBasedResourceSnapshotRepository.count()

    private fun TemplateBasedResourceSnapshot<*>.toTemplateBasedResourceSnapshotEntity(): TemplateBasedResourceSnapshotEntity =
        postgresTemplateBasedResourceSnapshotRepository.findById(id.value).orElseGet(::TemplateBasedResourceSnapshotEntity).apply {
            id = this@toTemplateBasedResourceSnapshotEntity.id.value
            createdBy = this@toTemplateBasedResourceSnapshotEntity.createdBy.value
            createdAt = this@toTemplateBasedResourceSnapshotEntity.createdAt
            data = objectMapper.valueToTree(this@toTemplateBasedResourceSnapshotEntity.data)
            modelVersion = this@toTemplateBasedResourceSnapshotEntity.modelVersion
            resourceId = this@toTemplateBasedResourceSnapshotEntity.resourceId.value
            templateId = this@toTemplateBasedResourceSnapshotEntity.templateId.value
            handle = this@toTemplateBasedResourceSnapshotEntity.handle?.value
        }
}
