package org.orkg.mediastorage.output

import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId
import java.util.Optional

interface ImageRepository {
    fun save(image: Image)

    fun findById(id: ImageId): Optional<Image>

    fun nextIdentity(): ImageId
}
