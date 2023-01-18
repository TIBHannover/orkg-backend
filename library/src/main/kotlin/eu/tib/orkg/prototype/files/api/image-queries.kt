package eu.tib.orkg.prototype.files.api

import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageId
import java.util.*

interface RetrieveImageUseCase {
    fun find(id: ImageId): Optional<Image>
}
