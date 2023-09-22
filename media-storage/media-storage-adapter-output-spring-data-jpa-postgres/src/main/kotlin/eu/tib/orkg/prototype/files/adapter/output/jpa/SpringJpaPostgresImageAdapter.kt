package eu.tib.orkg.prototype.files.adapter.output.jpa

import eu.tib.orkg.prototype.files.adapter.output.jpa.internal.ImageEntity
import eu.tib.orkg.prototype.files.adapter.output.jpa.internal.PostgresImageRepository
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.files.spi.ImageRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class SpringJpaPostgresImageAdapter(
    private val repository: PostgresImageRepository
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
        }
}
