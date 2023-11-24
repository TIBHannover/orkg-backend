package org.orkg.mediastorage.input

import javax.activation.MimeType
import org.orkg.common.ContributorId
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId

interface CreateImageUseCase {
    fun create(command: CreateCommand): ImageId

    data class CreateCommand(
        val data: ImageData,
        val mimeType: MimeType,
        val createdBy: ContributorId?
    )
}
