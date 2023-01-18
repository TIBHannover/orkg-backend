package eu.tib.orkg.prototype.files.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import javax.activation.MimeType

interface CreateImageUseCase {
    fun create(command: CreateCommand): ImageId

    data class CreateCommand(
        val data: ImageData,
        val mimeType: MimeType,
        val createdBy: ContributorId
    )
}
