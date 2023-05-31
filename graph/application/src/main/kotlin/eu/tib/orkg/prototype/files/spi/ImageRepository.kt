package eu.tib.orkg.prototype.files.spi

import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import java.util.*

interface ImageRepository {
    fun save(image: Image)
    fun findById(id: ImageId): Optional<Image>
    fun nextIdentity(): ImageId
}
