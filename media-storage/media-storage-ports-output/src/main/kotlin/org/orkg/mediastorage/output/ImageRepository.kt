package org.orkg.mediastorage.output

import java.util.*
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId

interface ImageRepository {
    fun save(image: Image)
    fun findById(id: ImageId): Optional<Image>
    fun nextIdentity(): ImageId
}
