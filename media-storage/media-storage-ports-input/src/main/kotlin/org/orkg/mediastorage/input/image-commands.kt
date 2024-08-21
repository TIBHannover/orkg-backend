package org.orkg.mediastorage.input

import org.orkg.common.ContributorId
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId
import jakarta.activation.MimeType

interface CreateImageUseCase {
    fun create(command: CreateCommand): ImageId

    data class CreateCommand(
        val data: ImageData,
        val mimeType: MimeType,
        val createdBy: ContributorId?
    )
}
