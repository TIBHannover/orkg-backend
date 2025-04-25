package org.orkg.mediastorage.adapter.output.jpa

import org.orkg.mediastorage.adapter.output.jpa.internal.ImageEntity
import org.orkg.mediastorage.adapter.output.jpa.internal.PostgresImageRepository
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.output.ImageRepository
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class SpringDataJpaImageAdapter(
    private val repository: PostgresImageRepository,
) : ImageRepository {
    override fun save(image: Image) {
        repository.save(image.toImageEntity())
    }

    override fun findById(id: ImageId): Optional<Image> =
        repository.findById(id.value).map(ImageEntity::toImage)

    override fun nextIdentity(): ImageId {
        var uuid: UUID?
        do {
            uuid = UUID.randomUUID()
        } while (repository.existsById(uuid!!))
        return ImageId(uuid)
    }

    private fun Image.toImageEntity(): ImageEntity =
        repository.findById(id.value).orElse(ImageEntity()).apply {
            id = this@toImageEntity.id.value
            data = this@toImageEntity.data.bytes
            mimeType = this@toImageEntity.mimeType.toString()
            createdBy = this@toImageEntity.createdBy?.value
            createdAt = this@toImageEntity.createdAt
            createdAtOffsetTotalSeconds = this@toImageEntity.createdAt.offset.totalSeconds
        }
}
