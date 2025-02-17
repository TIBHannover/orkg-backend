package org.orkg.mediastorage.input

import java.util.*
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageId

interface RetrieveImageUseCase {
    fun findById(id: ImageId): Optional<Image>
}
